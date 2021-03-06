package dev.tricht.gamesense

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.tricht.gamesense.com.steelseries.ApiClient
import dev.tricht.gamesense.com.steelseries.model.*
import dev.tricht.gamesense.model.*
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.io.File
import java.util.*
import javax.swing.ImageIcon
import kotlin.system.exitProcess

val mapper = jacksonObjectMapper()
const val GAME_NAME = "GAMESENSE_ESSENTIALS"
const val CLOCK_EVENT = "CLOCK"
const val VOLUME_EVENT = "VOLUME"
const val SONG_EVENT = "SONG"

fun main() {
    setupSystemtray()
    val address = getGamesenseAddress()
    val retrofit = Retrofit.Builder()
        .baseUrl("http://$address")
        .addConverterFactory(JacksonConverterFactory.create(mapper))
        .build()
    val client = retrofit.create(ApiClient::class.java)
    registerHandlers(client)
    val timer = Timer()
    timer.schedule(EventProducer(client), 0, 50)
}

private fun setupSystemtray() {
    if (!SystemTray.isSupported()) {
        ErrorUtil.showErrorDialogAndExit("System is not supported.");
        return
    }
    val tray = SystemTray.getSystemTray()
    val menu = PopupMenu("Gamesense Essentials")
    val title = MenuItem("Gamesense Essentials")
    title.isEnabled = false
    val exit = MenuItem("Exit")
    menu.add(title)
    menu.add(exit)
    exit.addActionListener { exitProcess(0) }
    val icon =
        TrayIcon(ImageIcon(EventProducer::class.java.classLoader.getResource("icon.png"), "Gamesense Essentials").image)
    icon.isImageAutoSize = true
    icon.popupMenu = menu
    tray.add(icon)
}

fun getGamesenseAddress(): String {
    val json = File("C:\\ProgramData\\SteelSeries\\SteelSeries Engine 3\\coreProps.json").readText(Charsets.UTF_8)
    val props: Props = mapper.readValue(json)
    return props.address
}

fun registerHandlers(client: ApiClient) {
    val clockHandler = EventRegistration(
        GAME_NAME,
        CLOCK_EVENT,
        listOf(
            Handler(
                listOf(
                    HandlerData(
                        iconId = 15
                    )
                )
            )
        )
    )
    var response = client.addEvent(clockHandler).execute()
    if (!response.isSuccessful) {
        println("Failed to add clock handler, error: " + response.errorBody()?.string())
        exitProcess(1)
    }
    val volumeHandler = EventRegistration(
        GAME_NAME,
        VOLUME_EVENT,
        listOf(
            Handler(
                listOf(
                    MultiLine(
                        listOf(
                            HandlerData(
                                // Currently sets the arg to '()' instead of nothing
                                arg = "",
                                // So we fix that by adding some spaces on a prefix...
                                prefix = "Volume" + " ".repeat(20)
                            ),
                            HandlerData(
                                hasProgressBar = true,
                                hasText = false
                            )
                        ),
                        23
                    )
                )
            )
        )
    )
    response = client.addEvent(volumeHandler).execute()
    if (!response.isSuccessful) {
        println("Failed to add volume handler, error: " + response.errorBody()?.string())
        exitProcess(1)
    }
    val songHandler = EventRegistration(
        GAME_NAME,
        SONG_EVENT,
        listOf(
            Handler(
                listOf(
                    MultiLine(
                        listOf(
                            HandlerData(
                                contextFrameKey = "artist"
                            ),
                            HandlerData(
                                contextFrameKey = "song"
                            )
                        ),
                        23
                    )
                )
            )
        ),
        listOf(
            DataField(
                "artist",
                "Arist"
            ),
            DataField(
                "song",
                "Song"
            )
        )
    )
    response = client.addEvent(songHandler).execute()
    if (!response.isSuccessful) {
        println("Failed to add song handler, error: " + response.errorBody()?.string())
        exitProcess(1)
    }
}
