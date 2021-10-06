package util

import com.example.ukromap.R
import utilrecycle.Avatar

class AvatarManager {

    companion object{

        var DEFAULT_MAN_AVATAR = 1000
        var DEFAULT_WOMAN_AVATAR = 1001
        var SPORTWOMAN_AVATAR = 1004
        var SOLDIER_AVATAR = 1002
        var NICK_FURY_AVATAR = 1005
        var RAPPER_AVATAR = 1003
        var RICK_AVATAR = 1006


        var avatars:Map<Int, Int> = mapOf(DEFAULT_MAN_AVATAR to R.drawable.default_man_avatar, DEFAULT_WOMAN_AVATAR to R.drawable.default_woman_avatar, SOLDIER_AVATAR to R.drawable.soldier_avatar, RAPPER_AVATAR to
            R.drawable.rapper_avatar, SPORTWOMAN_AVATAR to R.drawable.sportwoman_avatar, NICK_FURY_AVATAR to R.drawable.fury_avatar, RICK_AVATAR to R.drawable.rick_avatar
        )

        var listAvatars = listOf(
            Avatar(DEFAULT_MAN_AVATAR, 0, "Чоловік", "Звичайний чоловік. Прокидається о шостій, їде на роботу, повертається о дев'ятій. Полюбляє футбол і бити жінку."),
            Avatar(DEFAULT_WOMAN_AVATAR, 0, "Жінка", "Любить чоловіка доки в нього є гроші. Є запасний варіант у виді закоханого однокласника. "),
            Avatar(SPORTWOMAN_AVATAR, 3, "Спортсменка", "Також встає о шостій. Бігає дві години зранку, половину часу знімає історії в інстаграм. ") ,
            Avatar(SOLDIER_AVATAR, 5, "Солдат", "Захищає Батьківщину. Інколи не розуміє, чи варта вона захисту. "),
            Avatar(NICK_FURY_AVATAR, 10, "Нік Ф'юрі", "Голова таємної організації. Полюбляє пончики. "),
            Avatar(RAPPER_AVATAR, 15, "Репер", "Відкривання його рота називають співанням. Дай Боже, щоб дожив до 27. "),
            Avatar(RICK_AVATAR, 25, "Рік", "Професор - геній.")

        )

    }

}