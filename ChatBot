import com.github.jnexil.skribe.*
import com.github.jnexil.skribe.test.*
import com.mrkostua.math.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*

// Import the relevant Gemini classes
import com.adacore.gemini.*

import kotlinx.coroutines.flow.mapNotNull

class MyBot : Application() {

    // Initialize Firebase
    private val db = initializeFirebase()

    // Define command handlers
    init {
        command("/start") { start(it) }
        command("/help") { helpCommand(it) }
        message { locationHandler(it) }
    }

    private fun initializeFirebase(): Firestore {
        // Initialize Firebase logic here
        // ...

        return firestore // Assuming you have a Firestore instance
    }

    private suspend fun start(update: Update) {
        val user = update.user
        val message = """
            Hi ${user.mentionHtml()}! 👋
            <b><i>I'm Ada</i></b>, your friendly chat bot 🤖!
            
            I'm here to help you <b><i>discover social events, community initiatives, and drives</i></b> happening around you.
            Whether you're looking for <b><i>volunteer opportunities, charity events, or community gatherings</i></b>, I've got you covered!
            
            Just share your <b><i>location</i></b> or <b><i>community name</i></b> with me, and I'll fetch the <b><i>latest happenings</i></b> in your area.
            (<i>we are restricted with tasks, so kindly provide only your location in a word or so</i>)
            
            <b><i>Let's explore together!</i></b> 🌍✨
        """.trimIndent()

        update.message.replyHtml(message, replyMarkup = ForceReply(selective = true))
    }

    private suspend fun helpCommand(update: Update) {
        update.message.replyText("Help!")
    }

    private suspend fun locationHandler(update: Update) {
        val userMessage = update.message.text.capitalize()

        val collectionRef = db.collection("posts")

        val cityAttribute = "communityName"
        val cityOperation = "=="

        // Query documents where the 'communityName' field contains the user's message
        val documents = collectionRef.whereEqualTo(cityAttribute, userMessage).limit(5).get().await()

        // Prepare a reply message with the fetched data
        var replyText = ""
        for (document in documents) {
            val title = document.getString("title")?.capitalize() ?: ""
            val community = document.getString("communityName")?.capitalize() ?: ""

            var description = document.getString("description")?.capitalize() ?: ""
            if (description.isNotBlank()) {
                description = "ℹ️ Description: $description\n"
            }

            val username = document.getString("username")?.capitalize() ?: ""

            replyText += """
                <b>📌 Title:</b> $title
                <b>🌍 Community:</b> $community
                $description
                <b>👤 Username:</b> $username
                
            """.trimIndent()
        }

        if (replyText.isBlank()) {
            replyText =
                "<b>No data found for the given location.</b>\n\n<i>Probably you've entered something mismatched, kindly try with something else.</i>"
        } else {
            replyText = "<b>Here's a list of some exciting social drives happening in $userMessage! 🌟</b>\n\n$replyText"
        }

        // Split the replyText into chunks of maximum message length allowed by Telegram
        val messageChunks = replyText.chunked(4096)

        // Reply to the user with each chunk separately
        messageChunks.forEach {
            update.message.replyHtml(it)
        }
    }
}

fun main() {
    MyBot().run()
}

