package tasklist
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

@JsonClass(generateAdapter = true)
data class Task(var priority: String,
                var date: String,
                var time: String,
                var due: String,
                var tasks: MutableList<String> = emptyList<String>().toMutableList()
)

fun main() {

    val scanner = Scanner(System.`in`)
    var priorityOfTask = ""
    var dateOfTask = ""
    var timeOfTask = ""
    var dueTag = ""
    var oneTaskList = mutableListOf<String>()
    var deleteTaskNumber = Int.MAX_VALUE
    var editTaskNumber = Int.MAX_VALUE
    var fieldToEdit: String
    val jsonFile = File("tasklist.json")
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    var allTasks = mutableListOf<Task>()

    val type = Types.newParameterizedType(MutableList::class.java, Task::class.java)
    val taskListAdapter: JsonAdapter<MutableList<Task>> = moshi.adapter(type)

    fun end() {
        jsonFile.writeText(taskListAdapter.toJson(allTasks))
        println("Tasklist exiting!")
        exitProcess(1)
    }

    fun addPriority() {
        val priorities = mutableListOf("C", "H", "N", "L")
        while (priorityOfTask.isEmpty()) {
            println("Input the task priority (C, H, N, L):")
            priorityOfTask = readln().uppercase()
            if (!priorities.contains(priorityOfTask)) priorityOfTask = ""
        }
    }

    fun addDate() {
        while (dateOfTask.isEmpty()) {
            println("Input the date (yyyy-mm-dd):")
            try {
                val inputDate = readln()
                val formatter = DateTimeFormatter.ofPattern("yyyy-M-d", Locale.getDefault())
                dateOfTask = LocalDate.parse(inputDate, formatter).toString()
                if (inputDate.substringAfterLast('-').length == 2) {
                    LocalDate.parse(dateOfTask.substring(0,8)+ inputDate.substringAfterLast('-'))
                } else {
                    LocalDate.parse(dateOfTask.substring(0,8)+"0"+ inputDate.substringAfterLast('-'))
                }
            }
            catch (e:Exception) {
                println("The input date is invalid")
                dateOfTask = ""
            }
        }
    }

    fun addTime() {
        while (timeOfTask.isEmpty()) {
            println("Input the time (hh:mm):")
            val inputTime = readln()
            timeOfTask = try {
                if (inputTime == "24:00") throw Exception()
                val formatter = DateTimeFormatter.ofPattern("H:m", Locale.getDefault())
                LocalTime.parse(inputTime,formatter).toString()
            } catch (e: Exception) {
                println("The input time is invalid")
                ""
            }
        }
    }

    fun addTag() {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(dateOfTask.toLocalDate())
        dueTag = when {
            numberOfDays == 0 -> "T"
            numberOfDays > 0 -> "I"
            else -> "O"
        }
    }

    fun addTask() {
        println("Input a new task (enter a blank line to end):")
        var inputLine = scanner.nextLine()
        if (inputLine.isBlank()) {
            println("The task is blank")
        } else {
            oneTaskList.add(inputLine.trim())
            while (inputLine.isNotBlank()) {
                inputLine = scanner.nextLine()
                oneTaskList.add(inputLine.trim())
            }
        }
    }

    fun add() {
        addPriority()
        addDate()
        addTime()
        addTag()
        addTask()
        val newTask = Task(priorityOfTask, dateOfTask, timeOfTask, dueTag)
        newTask.tasks = oneTaskList
        allTasks.add(newTask)
        priorityOfTask = ""
        dateOfTask = ""
        timeOfTask = ""
        dueTag = ""
        oneTaskList = emptyList<String>().toMutableList()
    }

    fun printTasks() {
        val printTable = """+----+------------+-------+---+---+--------------------------------------------+
| N  |    Date    | Time  | P | D |                   Task                     |
+----+------------+-------+---+---+--------------------------------------------+"""
        val red = "\u001B[101m \u001B[0m"
        val green = "\u001B[102m \u001B[0m"
        val yellow = "\u001B[103m \u001B[0m"
        val blue = "\u001B[104m \u001B[0m"
        if (allTasks.isEmpty()) {
            println("No tasks have been input")
        } else {
            println(printTable)
            for (i in allTasks.indices) {
                val priorityColor = when (allTasks[i].priority) {
                    "C" -> red
                    "H" -> yellow
                    "N" -> green
                    "L" -> blue
                    else -> ""
                }
                val dueColor = when (allTasks[i].due) {
                    "O" -> red
                    "T" -> yellow
                    "I" -> green
                    else -> ""
                }
                val infoLine = "| ${i + 1}  | ${allTasks[i].date} | ${allTasks[i].time} | " + priorityColor + " | " + dueColor + " |"
                val lineOfOutput = "|    |            |       |   |   |"
                if (allTasks[i].tasks[0].length <= 44) {
                    println(infoLine + allTasks[i].tasks[0].padEnd(44) + "|")
                } else {
                    println(infoLine + allTasks[i].tasks[0].substring(0, 44) + "|")
                    val repeatingTimes = (allTasks[i].tasks[0].length - 44) / 44
                    repeat (repeatingTimes) {
                        for (y in 44..allTasks[i].tasks[0].lastIndex step 44) {
                            println(lineOfOutput + allTasks[i].tasks[0].substring(y, y + 44).padEnd(44) + "|")
                        }
                    }
                    if (allTasks[i].tasks[0].length % 44 != 0) println(lineOfOutput + allTasks[i].tasks[0].substring(44 + repeatingTimes * 44, allTasks[i].tasks[0].lastIndex + 1).padEnd(44) + "|")
                }
                for (x in 1 until allTasks[i].tasks.lastIndex) {
                    if (allTasks[i].tasks[x].length <= 44) {
                        println(lineOfOutput + allTasks[i].tasks[x].padEnd(44) + "|")
                    }
                    else {
                        println(lineOfOutput + allTasks[i].tasks[x].substring(0, 44) + "|")
                        val repeatingTimes = (allTasks[i].tasks[x].length - 44) / 44
                        repeat (repeatingTimes) {
                            for (y in 44..allTasks[i].tasks[x].lastIndex step 44) {
                                println(lineOfOutput + allTasks[i].tasks[x].substring(y, y + 44).padEnd(44) + "|")
                            }
                        }
                        if (allTasks[i].tasks[x].length % 44 != 0) println(lineOfOutput + allTasks[i].tasks[x].substring(44 + repeatingTimes * 44, allTasks[i].tasks[x].lastIndex + 1).padEnd(44) + "|")
                    }
                }
                println("+----+------------+-------+---+---+--------------------------------------------+")
            }
        }
    }

    fun delete() {
        printTasks()
        if (allTasks.isEmpty()) return
        while (deleteTaskNumber == Int.MAX_VALUE) {
            println("Input the task number (1-${allTasks.lastIndex + 1}):")
            try {
                deleteTaskNumber = readln().toInt()
                if (deleteTaskNumber !in 1..allTasks.lastIndex + 1) throw Exception()
            } catch (e: Exception) {
                println("Invalid task number")
                deleteTaskNumber = Int.MAX_VALUE
            }
        }
        allTasks.removeAt(deleteTaskNumber - 1)
        deleteTaskNumber = Int.MAX_VALUE
        println("The task is deleted")
    }

    fun edit() {
        printTasks()
        if (allTasks.isEmpty()) return
        while (editTaskNumber == Int.MAX_VALUE) {
            println("Input the task number (1-${allTasks.lastIndex + 1}):")
            try {
                editTaskNumber = readln().toInt()
                if (editTaskNumber !in 1..allTasks.lastIndex + 1) throw Exception()
            } catch (e: Exception) {
                println("Invalid task number")
                editTaskNumber = Int.MAX_VALUE
            }
        }
        fieldToEdit = ""
        val listOfFields = mutableListOf("priority", "date", "time", "task")
        while (fieldToEdit.isEmpty()) {
            println("Input a field to edit (priority, date, time, task):")
            fieldToEdit = readln().lowercase()
            if (!listOfFields.contains(fieldToEdit)) {
                println("Invalid field")
                fieldToEdit = ""
            }
        }
        when (fieldToEdit) {
            "priority" -> {
                priorityOfTask = ""
                addPriority()
                allTasks[editTaskNumber - 1].priority = priorityOfTask
            }
            "date" -> {
                dateOfTask = ""
                addDate()
                allTasks[editTaskNumber - 1].date = dateOfTask
                addTag()
                allTasks[editTaskNumber - 1].due = dueTag
            }
            "time" -> {
                timeOfTask = ""
                addTime()
                allTasks[editTaskNumber - 1].time = timeOfTask
            }
            else -> {
                allTasks[editTaskNumber - 1].tasks.clear()
                println("Input a new task (enter a blank line to end):")
                var inputLine = scanner.nextLine()
                if (inputLine.isBlank()) {
                    println("The task is blank")
                } else {
                    oneTaskList.add(inputLine.trim())
                    while (inputLine.isNotBlank()) {
                        inputLine = scanner.nextLine()
                        oneTaskList.add(inputLine.trim())
                    }
                    allTasks[editTaskNumber - 1].tasks.addAll(oneTaskList)
                }
            }
        }
        fieldToEdit = ""
        println("The task is changed")
        editTaskNumber = Int.MAX_VALUE
    }

    if (jsonFile.exists()) {
        val listFromJson = taskListAdapter.fromJson(jsonFile.readText(Charsets.UTF_8))
        if (listFromJson != null) {
            allTasks = listFromJson
        }
    }

    while (true) {
        println("Input an action (add, print, edit, delete, end):")
        when (readln().lowercase()) {
            "end" -> end()
            "add" -> add()
            "print" -> printTasks()
            "edit" -> edit()
            "delete" -> delete()
            else -> println("The input action is invalid")
        }
    }
}