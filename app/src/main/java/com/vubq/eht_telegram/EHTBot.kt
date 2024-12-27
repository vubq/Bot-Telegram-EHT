package com.vubq.ehttelegram

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.vubq.ehttelegram.enums.AutoType
import com.vubq.ehttelegram.enums.EquipmentType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EHTBot(private val telegramBot: TelegramBot) {

    private var pathData: String = "/storage/emulated/0/AutoEHT/"

    private var auto: Boolean = false

    private var autoType: AutoType = AutoType.NULL;

    private var equipmentType: EquipmentType = EquipmentType.NULL

    private var presetB: Boolean = false;

    private var strengthenPlace: Int? = null;

    private var eraseAttributePlace: Int? = null;

    fun setAuto(auto: Boolean) {
        this.auto = auto
    }

    fun setAutoType(autoType: AutoType) {
        this.autoType = autoType
    }

    fun setEquipmentType(equipmentType: EquipmentType) {
        this.equipmentType = equipmentType
    }

    fun setPresetB(presetB: Boolean) {
        this.presetB = presetB
    }

    fun setStrengthenPlace(strengthenPlace: Int?) {
        this.strengthenPlace = strengthenPlace
    }

    private fun String.adbExecution(delay: Long) {
        if (!auto) return
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", this))
        process.waitFor()
        Thread.sleep(delay)
    }

    private fun String.openApp(delay: Long) {
        "monkey -p $this -c android.intent.category.LAUNCHER 1".adbExecution(delay)
    }

    private fun click(x: Int, y: Int, delay: Long) {
        "input tap $x $y".adbExecution(delay)
    }

    private fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, speed: Int = 500, delay: Long) {
        "input swipe $x1 $y1 $x2 $y2 $speed".adbExecution(delay)
    }

    private fun String.screenCapture(delay: Long) {
        "screencap -p $pathData$this.png".adbExecution(delay)
    }

    private fun adjustBrightness(i: Int, delay: Long) {
        "shell settings put system screen_brightness $i".adbExecution(delay)
    }

    private fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(calendar.time)
    }

    private fun unicode(text: String): String {
        val diacriticMap = mapOf(
            'á' to 'a', 'à' to 'a', 'ả' to 'a', 'ã' to 'a', 'ạ' to 'a',
            'ă' to 'a', 'ắ' to 'a', 'ằ' to 'a', 'ẳ' to 'a', 'ẵ' to 'a', 'ặ' to 'a',
            'â' to 'a', 'ấ' to 'a', 'ầ' to 'a', 'ẩ' to 'a', 'ẫ' to 'a', 'ậ' to 'a',
            'é' to 'e', 'è' to 'e', 'ẻ' to 'e', 'ẽ' to 'e', 'ẹ' to 'e',
            'ê' to 'e', 'ế' to 'e', 'ề' to 'e', 'ể' to 'e', 'ễ' to 'e', 'ệ' to 'e',
            'í' to 'i', 'ì' to 'i', 'ỉ' to 'i', 'ĩ' to 'i', 'ị' to 'i',
            'ó' to 'o', 'ò' to 'o', 'ỏ' to 'o', 'õ' to 'o', 'ọ' to 'o',
            'ô' to 'o', 'ố' to 'o', 'ồ' to 'o', 'ổ' to 'o', 'ỗ' to 'o', 'ộ' to 'o',
            'ơ' to 'o', 'ớ' to 'o', 'ờ' to 'o', 'ở' to 'o', 'ỡ' to 'o', 'ợ' to 'o',
            'ú' to 'u', 'ù' to 'u', 'ủ' to 'u', 'ũ' to 'u', 'ụ' to 'u',
            'ư' to 'u', 'ứ' to 'u', 'ừ' to 'u', 'ử' to 'u', 'ữ' to 'u', 'ự' to 'u',
            'ý' to 'y', 'ỳ' to 'y', 'ỷ' to 'y', 'ỹ' to 'y', 'ỵ' to 'y',
            'Đ' to 'D', 'đ' to 'd'
        )
        return text.map { diacriticMap[it] ?: it }.joinToString("")
    }

    private suspend fun recognizeText(image: String): String {
        val inputStream: InputStream = File(image).inputStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val visionText = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await()
        return unicode(visionText.text)
    }

    private fun getTextFromImage(
        fileName: String,
        comparativeWords: List<String>,
        bout: Int,
    ): Boolean = runBlocking {
        val text: String = recognizeText("$pathData$fileName.png")

        if (text.isEmpty()) return@runBlocking false

        val exist = comparativeWords.any { text.contains(it, ignoreCase = true) }

//        telegramBot.sendMessage("$text - $exist")

        val textToAppend = "Lần $bout: $text - $exist" + " - " + getCurrentDateTime()

        val file = File("$pathData$fileName.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        BufferedWriter(FileWriter("$pathData$fileName.txt", true)).use { writer ->
            writer.write(textToAppend)
            writer.newLine()
        }

        return@runBlocking exist
    }

    private fun cropImage(fileName: String, x: Int, y: Int, width: Int, height: Int) {
        val filePath = "$pathData$fileName.png"
        val inputStream: InputStream = File(filePath).inputStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)

        val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height)

        val outputFile = File(filePath)
        val outputStream = FileOutputStream(outputFile)
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }

    fun screenCapture() {
        "test".screenCapture(0)
    }

    fun readFile(fileName: String): String {
        val file = File("$pathData$fileName.txt")
        if (!file.exists()) {
            return "Không có file!"
        } else {
            return file.readText()
        }
    }

    fun clearFile(fileName: String): String {
        val file = File("$pathData$fileName.txt")
        if (!file.exists()) {
            return "Không có file!"
        } else {
            file.writeText("")
            return "Đã clear file!"
        }
    }

    private fun initAuto() {
        //Mở App Backup
        "com.machiav3lli.backup".openApp(500)

        //Nhấn khôi phục
        click(409, 895, 500)

        //Nhấn OK
        click(460, 656, 2000)

        //Mở EHT
        "com.superplanet.evilhunter".openApp(17000)

        //Nhấn Touch To Start
        click(262, 817, 30000)

        //Nhấn đóng
        click(262, 729, 500)
    }

    private fun backup() {
        //Mở App Backup
        "com.machiav3lli.backup".openApp(500)

        //Nhấn sao lưu
        click(248, 1604, 500)

        //Nhấn bỏ APK
        click(121, 839, 500)

        //Nhấn OK
        click(939, 1643, 8000)
    }

    fun equip() {
        telegramBot.sendMessage("Start!")
        if (autoType == AutoType.NULL && equipmentType == EquipmentType.NULL) {
            telegramBot.sendMessage("Command error!")
            return
        }
        if (auto) {
            telegramBot.sendMessage("Auto đang chạy! \n Tắt auto trước khi thực hiện command khác!")
            return
        }
        auto = true
        Thread {
            while (auto) {
                initAuto()

                //Nhấn chọn lò rèn hoặc kim hoàn
                if (equipmentType == EquipmentType.NECKLACE || equipmentType == EquipmentType.RING) {
                    //Kim hoàn
                    click(340, 557, 500)
                } else {
                    //Lò rèn
                    click(210, 502, 500)
                }

                //Nhấn chọn loại đồ
                if (equipmentType == EquipmentType.ARMOR || equipmentType == EquipmentType.RING) {
                    //Giáp or nhẫn
                    click(153, 331, 500)
                }
                if (equipmentType == EquipmentType.GLOVES) {
                    //Găng
                    click(202, 334, 500)
                }
                if (equipmentType == EquipmentType.SHOE) {
                    //Giày
                    click(247, 333, 500)
                }

                //Nhấn chọn đồ
                if (equipmentType == EquipmentType.WEAPON) {
                    swipe(203, 594, 203, 359, 500, 0)
                    swipe(203, 594, 203, 359, 500, 0)
                    swipe(203, 594, 203, 359, 500, 0)
                    swipe(203, 594, 203, 359, 500, 500)

                    //Vũ khí
                    click(265, 580, 500)
                } else {
                    //Các đồ khác
                    click(392, 473, 500)
                }

                //Kéo đầy thanh
                swipe(135, 734, 485, 734, 500, 500)

                //Nhấn điều chế
                click(200, 828, 7000)

                //Nhấn tìm thuộc tính
                click(270, 326, 500)

                //Nhấn thiết lập sẵn A
                click(109, 152, 500)

                //Nhấn tìm kiếm
                click(182, 843, 2000)

                "Equip".screenCapture(0)

                if (!auto) break
                cropImage("Equip", 59, 307, 348 - 59, 349 - 307)

                if (!auto) break
                val comparativeWords = listOf("4 thuoc tinh co hieu luc")
                val isTrue = getTextFromImage("Equip", comparativeWords, 1)

                if (!auto) break
                if (isTrue) {
                    auto = false
                    telegramBot.sendMessage("Đã tìm thấy trang bị")
                    break
                }

                if (!presetB) continue

                //Nhấn xác nhận
                click(265, 863, 500)

                //Nhấn tìm thuộc tính
                click(270, 326, 500)

                //Nhấn thiết lập sẵn B
                click(228, 154, 500)

                //Nhấn tìm kiếm
                click(182, 843, 2000)

                "Equip".screenCapture(0)

                if (!auto) break
                cropImage("Equip", 59, 307, 348 - 59, 349 - 307)

                if (!auto) break
                val isTrue2 = getTextFromImage("Equip", comparativeWords, 2)

                if (!auto) break
                if (isTrue2) {
                    auto = false
                    telegramBot.sendMessage("Đã tìm thấy trang bị!")
                    break
                }
            }
        }.start()
    }

    fun strengthen() {
        telegramBot.sendMessage("Start!")
        if (autoType == AutoType.NULL && strengthenPlace == null) {
            telegramBot.sendMessage("Command error!")
            return
        }
        if (auto) {
            telegramBot.sendMessage("Auto đang chạy! \n Tắt auto trước khi thực hiện command khác!")
            return
        }
        auto = true
        Thread {
            while (auto) {
                initAuto()

                //Nhấn chọn cường hóa thần
                click(535, 990, 500)

                //Nhấn chọn ô
                if (strengthenPlace == 0) click(198, 1746, 500)
                if (strengthenPlace == 1) click(292, 1746, 500)
                if (strengthenPlace == 2) click(389, 1746, 500)
                if (strengthenPlace == 3) click(483, 1746, 500)
                if (strengthenPlace == 4) click(584, 1746, 500)
                if (strengthenPlace == 5) click(678, 1746, 500)
                if (strengthenPlace == 6) click(779, 1746, 500)
                if (strengthenPlace == 7) click(873, 1746, 500)

                "StrengthenMax".screenCapture(0)

                if (!auto) break
                cropImage("StrengthenMax", 109, 1262, 966 - 109, 1360 - 1262)

                if (!auto) break
                val isTrue =
                    getTextFromImage(
                        "StrengthenMax",
                        listOf("Khong the cuong hoa than them nua"),
                        1
                    )

                if (!auto) break
                if (isTrue) {
                    auto = false
                    telegramBot.sendMessage("Cường hóa max!")
                    break
                }

                //Nhấn cường hóa
                click(303, 2002, 7000)

                "Strengthen".screenCapture(0)

                if (!auto) break
                cropImage("Strengthen", 186, 762, 881 - 186, 876 - 762)

                if (!auto) break
                val isTrue2 = getTextFromImage("Strengthen", listOf("Cuong Hoa Thanh Cong"), 1)

                if (!auto) break
                if (isTrue2) backup()
            }
        }.start()
    }

    private fun eraseAttribute() {
        telegramBot.sendMessage("Start!")
        if (autoType == AutoType.NULL && eraseAttributePlace == null) {
            telegramBot.sendMessage("Command error!")
            return
        }
        if (auto) {
            telegramBot.sendMessage("Auto đang chạy! \n Tắt auto trước khi thực hiện command khác!")
            return
        }
        auto = true
        Thread {
            while (auto) {
                initAuto()

                //Nhấn chọn loại bỏ thuộc tính
                click(535, 990, 500)

                //Nhấn chọn ô
                if (eraseAttributePlace == 0) click(198, 1746, 500)
                if (eraseAttributePlace == 1) click(292, 1746, 500)
                if (eraseAttributePlace == 2) click(389, 1746, 500)
                if (eraseAttributePlace == 3) click(483, 1746, 500)
                if (eraseAttributePlace == 4) click(584, 1746, 500)
                if (eraseAttributePlace == 5) click(678, 1746, 500)
                if (eraseAttributePlace == 6) click(779, 1746, 500)
                if (eraseAttributePlace == 7) click(873, 1746, 500)

                "EraseAttributeMax".screenCapture(0)

                if (!auto) break
                cropImage("EraseAttributeMax", 125, 1490, 817, 87)

                if (!auto) break
                val isTrue =
                    getTextFromImage(
                        "EraseAttributeMax",
                        listOf("Khong co thuoc tinh am de loai bo"),
                        1
                    )

                if (!auto) break
                if (isTrue) {
                    auto = false
                    telegramBot.sendMessage("Đã loại bỏ thuộc tính âm!")
                    break
                }

                //Nhấn loại bỏ
                click(303, 2002, 7000)

                "EraseAttribute".screenCapture(0)

                if (!auto) break
                cropImage("EraseAttribute", 206, 783, 663, 85)

                if (!auto) break
                val isTrue2 =
                    getTextFromImage(
                        "EraseAttribute",
                        listOf("Da loai bo"),
                        1
                    )

                if (!auto) break
                if (isTrue2) backup()
            }
        }.start()
    }
}