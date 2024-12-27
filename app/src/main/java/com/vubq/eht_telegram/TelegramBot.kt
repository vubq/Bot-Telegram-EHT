package com.vubq.ehttelegram

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.vubq.ehttelegram.enums.AutoType
import com.vubq.ehttelegram.enums.EquipmentType

class TelegramBot {

    private val ehtBot: EHTBot = EHTBot(this)

    private val allowedUserIds = setOf(5543802102)

    val bot = bot {
        token = "7623992481:AAFROtN9qewiJpo-ypdlYlFWkhQSp0mpxbo"
        dispatch {
            text {
                handleTextCommand(text, message)
            }

            callbackQuery {
                handleCallBackQuery(callbackQuery)
            }
        }
    }

    private fun handleTextCommand(command: String, message: Message) {
        if (!isUserAllowed(message)) {
            bot.sendMessage(ChatId.fromId(message.chat.id), "Bạn không có quyền sử dụng bot này!")
        }
        val commands = command.split("_");
        when (commands[0]) {
            "/start" -> {
                bot.sendMessage(
                    ChatId.fromId(message.chat.id),
                    "Bot đã được khởi động! \n\n" +
                            "Command: \n\n" +
                            "Trang bị: \n" +
                            "/auto_Equip_Armor_B \n" +
                            "/auto_Equip_Gloves_B \n" +
                            "/auto_Equip_Shoe_B \n" +
                            "/auto_Equip_Necklace_B \n" +
                            "/auto_Equip_Ring_B \n" +
                            "/auto_Equip_Weapon_B \n" +
                            "/auto_Equip_Armor_A \n" +
                            "/auto_Equip_Gloves_A \n" +
                            "/auto_Equip_Shoe_A \n" +
                            "/auto_Equip_Necklace_A \n" +
                            "/auto_Equip_Ring_A \n" +
                            "/auto_Equip_Weapon_A \n\n" +
                            "Cường hóa: \n" +
                            "/auto_Strengthen_1 \n" +
                            "/auto_Strengthen_2 \n" +
                            "/auto_Strengthen_3 \n" +
                            "/auto_Strengthen_4 \n" +
                            "/auto_Strengthen_5 \n" +
                            "/auto_Strengthen_6 \n" +
                            "/auto_Strengthen_7 \n" +
                            "/auto_Strengthen_8 \n\n" +
                            "Đọc file txt: \n" +
                            "/readFile_Equip \n" +
                            "/readFile_Strengthen \n" +
                            "/readFile_StrengthenMax \n" +
                            "/readFile_EraseAttribute \n" +
                            "/readFile_EraseAttributeMax \n\n" +
                            "Clear file txt: \n" +
                            "/clearFile_Equip \n" +
                            "/clearFile_Strengthen \n" +
                            "/clearFile_StrengthenMax \n" +
                            "/clearFile_EraseAttribute \n" +
                            "/clearFile_EraseAttributeMax \n\n" +
                            "Dừng: \n" +
                            "/stopAuto \n\n"
                )
            }

            "/checkBot" -> {
                bot.sendMessage(ChatId.fromId(message.chat.id), "Bot đang hoạt động!")
            }

            "/stopAuto" -> {
                ehtBot.setAuto(false)
                bot.sendMessage(ChatId.fromId(message.chat.id), "Stop!")
            }

            "/auto" -> {
                when {
                    commands[1].equals("Equip", ignoreCase = true) -> {
                        ehtBot.setAutoType(AutoType.EQUIP)
                        when {
                            commands[2].equals("Armor", ignoreCase = true) -> {
                                ehtBot.setEquipmentType(EquipmentType.ARMOR)
                            }

                            commands[2].equals("Gloves", ignoreCase = true) -> {
                                ehtBot.setEquipmentType(EquipmentType.GLOVES)
                            }

                            commands[2].equals("Shoe", ignoreCase = true) -> {
                                ehtBot.setEquipmentType(EquipmentType.SHOE)
                            }

                            commands[2].equals("Necklace", ignoreCase = true) -> {
                                ehtBot.setEquipmentType(EquipmentType.NECKLACE)
                            }

                            commands[2].equals("Ring", ignoreCase = true) -> {
                                ehtBot.setEquipmentType(EquipmentType.RING)
                            }

                            commands[2].equals("Weapon", ignoreCase = true) -> {
                                ehtBot.setEquipmentType(EquipmentType.WEAPON)
                            }

                            else -> ehtBot.setEquipmentType(EquipmentType.NULL)
                        }
                        when {
                            commands[3].equals("B", ignoreCase = true) -> {
                                ehtBot.setPresetB(true)
                            }

                            else -> ehtBot.setPresetB(false)
                        }
                        ehtBot.equip()
                    }

                    commands[1].equals("Strengthen", ignoreCase = true) -> {
                        ehtBot.setAutoType(AutoType.STRENGTHEN)
                        when {
                            commands[2].equals("1", ignoreCase = true) -> {
                                ehtBot.setStrengthenPlace(1)
                            }

                            commands[2].equals("2", ignoreCase = true) -> {
                                ehtBot.setStrengthenPlace(2)
                            }

                            commands[2].equals("3", ignoreCase = true) -> {
                                ehtBot.setStrengthenPlace(3)
                            }

                            commands[2].equals("4", ignoreCase = true) -> {
                                ehtBot.setStrengthenPlace(4)
                            }

                            commands[2].equals("5", ignoreCase = true) -> {
                                ehtBot.setStrengthenPlace(5)
                            }

                            commands[2].equals("6", ignoreCase = true) -> {
                                ehtBot.setStrengthenPlace(6)
                            }

                            commands[2].equals("7", ignoreCase = true) -> {
                                ehtBot.setStrengthenPlace(7)
                            }

                            commands[2].equals("8", ignoreCase = true) -> {
                                ehtBot.setStrengthenPlace(8)
                            }

                            else -> ehtBot.setStrengthenPlace(null)
                        }
                        ehtBot.strengthen()
                    }

                    else -> ehtBot.setAutoType(AutoType.NULL)
                }
            }

            "/readFile" -> {
                bot.sendMessage(
                    ChatId.fromId(message.chat.id),
                    ehtBot.readFile(commands[1])
                )
            }

            "/clearFile" -> {
                bot.sendMessage(
                    ChatId.fromId(message.chat.id),
                    ehtBot.clearFile(commands[1])
                )
            }

            else -> {
                bot.sendMessage(
                    ChatId.fromId(message.chat.id),
                    "Bot đã được khởi động! \n\n" +
                            "Command: \n\n" +
                            "Trang bị: \n" +
                            "/auto_Equip_Armor_B \n" +
                            "/auto_Equip_Gloves_B \n" +
                            "/auto_Equip_Shoe_B \n" +
                            "/auto_Equip_Necklace_B \n" +
                            "/auto_Equip_Ring_B \n" +
                            "/auto_Equip_Weapon_B \n" +
                            "/auto_Equip_Armor_A \n" +
                            "/auto_Equip_Gloves_A \n" +
                            "/auto_Equip_Shoe_A \n" +
                            "/auto_Equip_Necklace_A \n" +
                            "/auto_Equip_Ring_A \n" +
                            "/auto_Equip_Weapon_A \n\n" +
                            "Cường hóa: \n" +
                            "/auto_Strengthen_1 \n" +
                            "/auto_Strengthen_2 \n" +
                            "/auto_Strengthen_3 \n" +
                            "/auto_Strengthen_4 \n" +
                            "/auto_Strengthen_5 \n" +
                            "/auto_Strengthen_6 \n" +
                            "/auto_Strengthen_7 \n" +
                            "/auto_Strengthen_8 \n\n" +
                            "Đọc file txt: \n" +
                            "/readFile_Equip \n" +
                            "/readFile_Strengthen \n" +
                            "/readFile_StrengthenMax \n" +
                            "/readFile_EraseAttribute \n" +
                            "/readFile_EraseAttributeMax \n\n" +
                            "Clear file txt: \n" +
                            "/clearFile_Equip \n" +
                            "/clearFile_Strengthen \n" +
                            "/clearFile_StrengthenMax \n" +
                            "/clearFile_EraseAttribute \n" +
                            "/clearFile_EraseAttributeMax \n\n" +
                            "Dừng: \n" +
                            "/stopAuto \n\n"
                )
            }
        }
    }

    private fun handleCallBackQuery(callbackQuery: CallbackQuery) {
        bot.sendMessage(ChatId.fromId(callbackQuery.from.id), "Call back")
    }

    private fun isUserAllowed(message: Message): Boolean {
        val userId = message.from?.id
        return userId != null && allowedUserIds.contains(userId)
    }

    fun sendMessage(message: String) {
        bot.sendMessage(ChatId.fromId(5543802102), message)
    }

    fun start() {
        bot.startPolling()
    }

//    fun stopNotice() {
//        bot.sendMessage(ChatId.fromId(5543802102), "Bot đã dừng!")
//    }
}