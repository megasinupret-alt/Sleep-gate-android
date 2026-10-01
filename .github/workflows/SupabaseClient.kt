package com.sleepgate.app

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class SupabaseClient {
    private fun request(method: String, path: String, body: String? = null, token: String? = null): String =
        (URL(SupabaseConfig.URL + path).openConnection() as HttpURLConnection).run {
            requestMethod = method
            setRequestProperty("apikey", SupabaseConfig.ANON_KEY)
            setRequestProperty("Authorization", "Bearer ${token ?: SupabaseConfig.ANON_KEY}")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Prefer", "return=representation")
            connectTimeout = 10000; readTimeout = 10000
            if (body != null) { doOutput = true; outputStream.use { it.write(body.toByteArray()) } }
            val code = responseCode
            val stream = if (code in 200..299) inputStream else errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) error("Supabase $code: $text")
            text
        }

    suspend fun signUp(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("email", email); put("password", password) }
        val out = request("POST", "/auth/v1/signup", json.toString())
        parseAuth(out)
    }
    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val json = JSONObject().apply { put("email", email); put("password", password) }
        val out = request("POST", "/auth/v1/token?grant_type=password", json.toString())
        parseAuth(out)
    }
    private fun parseAuth(raw: String): AuthResult {
        val o = JSONObject(raw)
        return AuthResult(o.optString("access_token").ifBlank { null }, o.optString("refresh_token").ifBlank { null }, o.optJSONObject("user")?.optString("id"))
    }

    suspend fun fetchTerminalId(iata:String="AUH", terminal:String="Terminal A"): String? = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.configured) return@withContext null
        val airports=JSONArray(request("GET","/rest/v1/airports?select=id&iata=eq.$iata&limit=1"))
        if(airports.length()==0) return@withContext null
        val aid=airports.getJSONObject(0).getString("id")
        val ts=JSONArray(request("GET","/rest/v1/terminals?select=id&airport_id=eq.$aid&name=eq.${java.net.URLEncoder.encode(terminal,"UTF-8")}&limit=1"))
        if(ts.length()==0) null else ts.getJSONObject(0).getString("id")
    }

    suspend fun fetchPlaces(): List<CloudPlace> = withContext(Dispatchers.IO) {
        if (!SupabaseConfig.configured) return@withContext emptyList()
        val json = request("GET", "/rest/v1/places?select=*")
        val arr = JSONArray(json)
        buildList { for (i in 0 until arr.length()) { val o=arr.getJSONObject(i); add(CloudPlace(
            id=o.getString("id"), title=o.getString("title"), area=o.optString("area"), canLie=o.optBoolean("can_lie"), power=o.optBoolean("power"),
            toiletDistance=if(o.isNull("toilet_distance_m")) null else o.optInt("toilet_distance_m"), shower=o.optBoolean("shower_nearby"), verified=o.optBoolean("verified"))) } }
    }

    suspend fun createPlace(place: NewCloudPlace, token: String): Boolean = withContext(Dispatchers.IO) {
        val body=JSONObject().apply { put("terminal_id",place.terminalId);put("title",place.title);put("area",place.area);put("can_lie",place.canLie);put("power",place.power);put("toilet_distance_m",place.toiletDistance ?: JSONObject.NULL);put("shower_nearby",place.shower);put("verified",false) }.toString()
        request("POST","/rest/v1/places",body,token); true
    }

    suspend fun addRating(placeId:String, userId:String, token:String, score:Int, comment:String):Boolean = withContext(Dispatchers.IO) {
        val body=JSONObject().apply { put("place_id",placeId);put("user_id",userId);put("comfort",score);put("noise",score);put("light",score);put("temperature",score);put("safety",score);put("privacy",score);put("amenities",score);put("cleanliness",score);put("comment",comment) }.toString()
        request("POST","/rest/v1/ratings",body,token); true
    }

    suspend fun uploadPhoto(uri:Uri, resolver:android.content.ContentResolver, placeId:String, userId:String, token:String):Boolean = withContext(Dispatchers.IO) {
        val bitmap=resolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it) } ?: return@withContext false
        val bytes=ByteArrayOutputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG,85,out); out.toByteArray() }
        val path="$userId/$placeId-${System.currentTimeMillis()}.jpg"
        val conn=URL("${SupabaseConfig.URL}/storage/v1/object/place-photos/$path").openConnection() as HttpURLConnection
        conn.requestMethod="POST";conn.doOutput=true;conn.setRequestProperty("apikey",SupabaseConfig.ANON_KEY);conn.setRequestProperty("Authorization","Bearer $token");conn.setRequestProperty("Content-Type","image/jpeg");conn.outputStream.use{it.write(bytes)}
        val code=conn.responseCode
        if(code !in 200..299) error("Storage $code: ${conn.errorStream?.bufferedReader()?.use{it.readText()} ?: ""}")
        val row=JSONObject().apply{put("place_id",placeId);put("user_id",userId);put("storage_path",path)}.toString()
        request("POST","/rest/v1/place_photos",row,token); true
    }
}

data class AuthResult(val accessToken:String?, val refreshToken:String?, val userId:String?)
data class CloudPlace(val id:String,val title:String,val area:String,val canLie:Boolean,val power:Boolean,val toiletDistance:Int?,val shower:Boolean,val verified:Boolean)
data class NewCloudPlace(val terminalId:String,val title:String,val area:String,val canLie:Boolean,val power:Boolean,val toiletDistance:Int?,val shower:Boolean)
