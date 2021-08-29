package com.example.routes

import com.example.data.model.Note
import com.example.data.model.SimpleResponse
import com.example.data.model.User
import com.example.repository.Repo
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

const val NOTES = "$API_VERSION/notes"
const val CREATE_NOTES = "$NOTES/create"
const val UPDATE_NOTES = "$NOTES/update"
const val DELETE_NOTES = "$NOTES/delete"

@Location(CREATE_NOTES)
class NoteCreateRoute

@Location(UPDATE_NOTES)
class NoteUpdateRoute

@Location(DELETE_NOTES)
class NoteDeleteRoute

@Location(NOTES)
class NoteGetRoute


fun Route.NoteRoutes(
    db:Repo,
    hashFunction:(String)-> String
){
    authenticate ("jwt"){

        post <NoteCreateRoute> {
            val note = try {
                call.receive<Note>()
            }catch (e:Exception){
                call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"Missing Fields"))
                return@post
            }

            try {
                val email = call.principal<User>()!!.email
                db.addNote(note,email)
                call.respond(HttpStatusCode.OK,SimpleResponse(true,"You have successfully saved the Note"))
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse(false,e.message ?: "Some problem occurred!"))
            }
        }

        get <NoteGetRoute>{
            try {
                val email = call.principal<User>()!!.email
                val notes = db.getAllNotes(email)
                call.respond(HttpStatusCode.OK,notes)
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict, emptyList<Note>())
            }
        }

        post <NoteUpdateRoute>{
            val note = try {
                call.receive<Note>()
            }catch (e:Exception){
                call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"Missing Fields"))
                return@post
            }

            try {
                val email = call.principal<User>()!!.email
                db.updateNote(note,email)
                call.respond(HttpStatusCode.OK,SimpleResponse(true,"You have successfully updated the Note"))
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,SimpleResponse(false,e.message ?: "Some problem occurred!"))
            }
        }

        delete <NoteDeleteRoute>{
            val noteId = try {
                call.request.queryParameters["id"]!!
            }catch (e:Exception){
                call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"id is not correct!"))
                return@delete
            }

            try {
                val email = call.principal<User>()!!.email
                db.deleteNote(noteId,email)
                call.respond(HttpStatusCode.OK,SimpleResponse(true,"Note has been deleted successfully"))
            }catch (e:Exception){
                call.respond(HttpStatusCode.Conflict,e.message ?: "Some Problem occurred")
            }
        }
    }
}