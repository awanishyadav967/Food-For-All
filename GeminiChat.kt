import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import com.google.gson.Gson

data class QueryRequest(
    val userMessage: String,
    val location: String? = null
)

data class QueryResponse(
    val reply: String,
    val relatedEvents: List<CommunityEvent> = emptyList()
)

data class CommunityEvent(
    val title: String,
    val community: String,
    val description: String,
    val username: String
)

class ChatBot {
    private val db: Firestore = initializeFirebase()
    private val generativeModel: GenerativeModel = initializeGemini()
    private val gson = Gson()

    private fun initializeFirebase(): Firestore {
        // Initialize Firebase (ensure Google credentials are set)
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp()
        }
        return FirestoreClient.getFirestore()
    }

    private fun initializeGemini(): GenerativeModel {
        // Make sure to set your GOOGLE_API_KEY environment variable
        val apiKey = System.getenv("GOOGLE_API_KEY") 
            ?: throw IllegalStateException("GOOGLE_API_KEY environment variable not set")
        return GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = apiKey
        )
    }

    suspend fun handleQuery(request: QueryRequest): QueryResponse {
        val userMessage = request.userMessage.trim()
        val location = request.location

        // Fetch relevant community events if location is provided
        val communityEvents = if (location != null) {
            fetchCommunityEvents(location)
        } else {
            emptyList()
        }

        // Build context for Gemini
        val context = buildContext(communityEvents, location)

        // Generate AI response using Gemini
        val aiResponse = generateResponse(userMessage, context)

        return QueryResponse(
            reply = aiResponse,
            relatedEvents = communityEvents
        )
    }

    private suspend fun fetchCommunityEvents(location: String): List<CommunityEvent> {
        return try {
            val collectionRef = db.collection("posts")
            val documents = collectionRef
                .whereEqualTo("communityName", location.replaceFirstChar { it.uppercase() })
                .limit(5)
                .get()
                .await()

            documents.map { document ->
                CommunityEvent(
                    title = document.getString("title")?.replaceFirstChar { it.uppercase() } ?: "",
                    community = document.getString("communityName")?.replaceFirstChar { it.uppercase() } ?: "",
                    description = document.getString("description")?.replaceFirstChar { it.uppercase() } ?: "",
                    username = document.getString("username")?.replaceFirstChar { it.uppercase() } ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun buildContext(events: List<CommunityEvent>, location: String?): String {
        return buildString {
            append("You are a helpful chatbot called Ada, designed to assist users with discovering social events, ")
            append("community initiatives, and volunteer opportunities.\n\n")

            if (location != null) {
                append("The user is interested in $location.\n")
            }

            if (events.isNotEmpty()) {
                append("Here are the latest community events and drives in the area:\n\n")
                events.forEach { event ->
                    append("- Title: ${event.title}\n")
                    append("  Community: ${event.community}\n")
                    append("  Description: ${event.description}\n")
                    append("  Organizer: ${event.username}\n\n")
                }
            } else if (location != null) {
                append("No specific events found for $location, but you can still provide general guidance.\n")
            }

            append("\nBe helpful, friendly, and encouraging. Suggest ways to get involved in community activities.")
        }
    }

    private suspend fun generateResponse(userMessage: String, context: String): String {
        return try {
            val prompt = """$context

User query: $userMessage

Please provide a helpful response that addresses the user's query. If they're looking for events, 
recommend the ones listed above if available. Keep the response concise and friendly."""

            val response = generativeModel.generateContent(prompt)
            response.text ?: "Sorry, I couldn't generate a response. Please try again."
        } catch (e: Exception) {
            "An error occurred while processing your request: ${e.message}"
        }
    }
}

fun main() {
    val chatBot = ChatBot()

    // Create a simple Ktor server for API endpoints
    embeddedServer(
        factory = Netty,
        port = 8080
    ) {
        routing {
            // Health check endpoint
            get("/health") {
                call.respond(HttpStatusCode.OK, mapOf("status" to "running"))
            }

            // Main query endpoint
            post("/api/chat") {
                try {
                    val request = call.receive<QueryRequest>()
                    
                    if (request.userMessage.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "User message cannot be empty")
                        )
                        return@post
                    }

                    val response = chatBot.handleQuery(request)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to process query: ${e.message}")
                    )
                }
            }

            // Endpoint to get events by location
            get("/api/events/{location}") {
                try {
                    val location = call.parameters["location"] ?: ""
                    val response = chatBot.handleQuery(
                        QueryRequest(
                            userMessage = "What events are happening here?",
                            location = location
                        )
                    )
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to fetch events: ${e.message}")
                    )
                }
            }
        }
    }.start(wait = true)
}
