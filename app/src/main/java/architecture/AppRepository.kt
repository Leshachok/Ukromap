package architecture

import android.accounts.NetworkErrorException
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.beust.klaxon.Klaxon
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.OptionalPendingResult
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import mapobjects.Sight
import mapobjects.SightManager
import mapobjects.SightMarker
import mapobjects.UkraineRegion
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpResponseException
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import serverRequests.*
import util.RestartManager
import utilrecycle.User
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import kotlin.collections.ArrayList


@Suppress("UNCHECKED_CAST")
class AppRepository(var context: Context) {

    companion object{
        private var friendsList:MutableList<User> = mutableListOf()
        private  var instance: AppRepository? = null

        fun getInstance(context: Context):AppRepository{
            if(instance==null){
                instance = AppRepository(context)
            }
            return instance as AppRepository
        }
    }

    fun downloadRegionsBorders() {
        UkraineRegion.listRegions.forEach { reg ->
                reg.bordersList = coordinatesFromUrl(reg.name, reg.url)
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun getAccountData(idToken: String?) {
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/login")

        try {
            var nameValuePairs = ArrayList<NameValuePair>(1)
            nameValuePairs.add(BasicNameValuePair("idToken", idToken))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)
            var result = Klaxon().parse<AccountRequestClass>(responseBody)
            var account:AccountClass = result!!.data.account

            var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var editor = preferences.edit()
            editor.putString("email", account.email)
            editor.putString("username", account.username)
            editor.putBoolean("newaccount", account.newaccount)
            editor.putString("id", account.id)
            editor.putInt("avatar", account.avatar)
            editor.putLong("last_nickname_change", account.last_nickname_change)

            editor.apply()


        }catch (e: IOException) {
            Log.e("IOException", "Error sending ID token to backend.", e)
            RestartManager.restart()
        }
    }

    fun getProfileName(): String {
        var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return preferences.getString("username", "undefined")!!
    }

    fun getVisitedNumber():Int{
        var preferences = context.getSharedPreferences("sights", Context.MODE_PRIVATE)
        return preferences.getStringSet("set", emptySet())?.size ?: 0
    }

    fun getFriends(reload: Boolean): MutableList<User> {
        if(!reload) return friendsList
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/friends")

        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(1)
            var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)

            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)
            var result = Klaxon().parse<FriendRequestClass>(responseBody)
            friendsList = result!!.data.toMutableList()
            var editor = preferences.edit()
            editor.putStringSet("friends", friendsList.stream().map { x->x.id }.collect(Collectors.toList()).toMutableSet())
            editor.apply()

        }catch (e: IOException) {
            Log.e("IOException", "Error sending request.", e)
            RestartManager.restart()
        }
        return friendsList
    }

    fun getSights(){
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/sights")
        try {
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            var result = Klaxon().parse<SightRequestClass>(responseBody)
            var list = result!!.data
            SightManager.listSights.clear()

            list.forEach { x->
                var marker = SightMarker(
                    x.longitude,
                    x.latitude,
                    encodeString(x.title),
                    encodeString(x.snippet),
                    resizeIcon(x.imagetitle)
                )
                Sight(marker, x.radius, x.guid)
            }

        }catch (e: IOException) {
            Log.e("IOException", "Error sending request.", e)
            RestartManager.restart()
        }
    }

    fun getVisitedSights(download: Boolean): MutableSet<String>? {
        var preferences = context.getSharedPreferences("sights", Context.MODE_PRIVATE)
        if(!download) return preferences.getStringSet("set", mutableSetOf())
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/visitedsights")

        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(1)
            preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)

            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)

            var result = Klaxon().parse<VisitedSightsClass>(responseBody)
            var list = result!!.data
            var set = list.stream().map { x->x.sight }.collect(Collectors.toList()).toSet()

            preferences = context.getSharedPreferences("sights", Context.MODE_PRIVATE)
            var editor = preferences.edit()
            editor.putStringSet("set", set)
            editor.apply()
        }catch (e:IOException){
            Log.e("IOException", "Error sending request.", e)
            RestartManager.restart()
        }
        return mutableSetOf()
    }

    @SuppressLint("ApplySharedPref")
    fun addVisitedSight(id: String){
        
        var preferences = context.getSharedPreferences("sights", Context.MODE_PRIVATE)
        var editor = preferences.edit()
        var set = preferences.getStringSet("set", mutableSetOf())!!.toMutableSet()
        set.add(id)
        editor.putStringSet("set", set)
        editor.commit()

        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/updatevisited")

        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(2)
            preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            nameValuePairs.add(BasicNameValuePair("sightId", id))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)
        }catch (e:IOException){
            Log.e("IOException", "Error sending request.", e)
            RestartManager.restart()
        }
    }

    private fun encodeString(s:String):String{
        var bytes = StandardCharsets.ISO_8859_1.encode(s)
        return String(bytes.array(), StandardCharsets.UTF_8)
    }

    private fun resizeIcon(title: String): BitmapDescriptor {
        var id = context.resources.getIdentifier(title, "drawable", context.packageName)
        val bitmap:Bitmap = BitmapFactory.decodeResource(context.resources, id)
        val smallMarker = Bitmap.createScaledBitmap(bitmap, 150, 150, false)
        return BitmapDescriptorFactory.fromBitmap(smallMarker)
    }

    private fun coordinatesFromUrl(regionName: String, url: String):List<LatLng>{
        var listPoints = emptyList<LatLng>()
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost(url)
        try {
            var nameValuePairs = ArrayList<NameValuePair>(1)
            nameValuePairs.add(BasicNameValuePair("regionname", regionName))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)

            var result = RequestResult.getResult(responseBody)

            listPoints = result.stream().map { x-> LatLng(x[1].toDouble(), x[0].toDouble()) }.collect(
                Collectors.toList()
            )
            var list = mutableListOf<LatLng>()
            listPoints.forEachIndexed { index, latLng ->
                if(index%3==0){
                    list.add(latLng)
                }
            }
            listPoints = list
        }catch (e: HttpResponseException){
            e.printStackTrace()
        }catch (e: IOException){
            Log.e("IOException", "Error sending request.", e)
            RestartManager.restart()
        }finally {
            return listPoints
        }

    }

    fun getUsersByNickname(nickName: String): MutableList<User> {
        var list = mutableListOf<User>()
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/user")

        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(2)
            var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            nameValuePairs.add(BasicNameValuePair("username", nickName))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)
            var result = Klaxon().parse<FriendRequestClass>(responseBody)
            list = result!!.data.toMutableList()
            var set = preferences.getStringSet("friends", mutableSetOf())
            set!!.add(preferences.getString("id", ""))
            Log.d("friends", set.toString())
            list = list.stream().filter { user-> !set.contains(user.id) }.collect(Collectors.toList())

        }catch (e:IOException){
            Log.d("Exception", e.toString())
        }
        return list
    }

    fun sendFriendRequest(id: String) {
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/addfriend")

        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(2)
            var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            nameValuePairs.add(BasicNameValuePair("guid", id))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)

        }catch (e:IOException){
            Log.d("Exception", e.toString())
        }
    }

    fun getFriendRequests(): MutableList<User> {
        var requests = mutableListOf<User>()
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/friendrequests")

        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(1)
            var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)
            var result = Klaxon().parse<FriendRequestClass>(responseBody)
            requests = result!!.data.toMutableList()
        }catch (e:IOException){
            Log.d("Exception", e.toString())
        }
        return requests
    }

    fun acceptFriendRequest(id: String) {
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/acceptfriendship")

        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(2)
            var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            nameValuePairs.add(BasicNameValuePair("guid", id))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)

            var set = preferences.getStringSet("friends", mutableSetOf())!!
            set.add(id)
            Log.d("newfriend", set.toString())
            preferences.edit().putStringSet("friends", set).apply()
        }catch (e:IOException){
            Log.d("Exception", e.toString())
        }
    }

    fun denyFriendRequest(id: String) {
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/denyfriendship")

        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(2)
            var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            nameValuePairs.add(BasicNameValuePair("guid", id))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)
        }catch (e:IOException){
            Log.d("Exception", e.toString())
        }
    }

    fun deleteFriend(id: String) {
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/deletefriend")

        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(2)
            var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            nameValuePairs.add(BasicNameValuePair("guid", id))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)

            var set = preferences.getStringSet("friends", mutableSetOf())!!
            set.remove(id)
            Log.d("deletedfriend", set.toString())
            preferences.edit().putStringSet("friends", set).apply()
        }catch (e:IOException){
            Log.d("Exception", e.toString())
        }
    }

    fun getAvatar(): Int {
        var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return preferences.getInt("avatar", 1)
    }

    fun changeNickname(name: String) {
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/changenickname")
        var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(3)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            nameValuePairs.add(BasicNameValuePair("nickname", name))
            nameValuePairs.add(BasicNameValuePair("time", getLastNicknameChangingTime().toString()))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)
        }catch (e:IOException){
            Log.d("Exception", e.toString())
        }
        var editor = preferences.edit()
        editor.putString("username", name)
        editor.apply()
    }

    fun getLastNicknameChangingTime(): Long{
        var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return preferences.getLong("last_nickname_change", 0L)
    }

    fun saveLastNicknameChangingTime(long: Long) {
        var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        var editor = preferences.edit()
        editor.putLong("last_nickname_change", long)
        editor.apply()
    }

    fun changeAvatar(avatar: Int) {
        var httpClient = DefaultHttpClient()
        var httpPost = HttpPost("https://stats.pnit.od.ua/ukromap/changeavatar")
        var preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        try {
            var nameValuePairs = java.util.ArrayList<NameValuePair>(2)
            var token = preferences.getString("idToken", "")
            nameValuePairs.add(BasicNameValuePair("idToken", token))
            nameValuePairs.add(BasicNameValuePair("avatar", avatar.toString()))
            httpPost.entity = UrlEncodedFormEntity(nameValuePairs)
            val response: HttpResponse = httpClient.execute(httpPost)
            val responseBody = EntityUtils.toString(response.entity)
            Log.d("response", responseBody)
        }catch (e:IOException){
            Log.d("Exception", e.toString())
            RestartManager.restart()
        }
    }

}