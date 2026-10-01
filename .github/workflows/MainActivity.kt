package com.sleepgate.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class Review(val author:String,val score:Int,val text:String)
private data class Place(val id:String,val title:String,val area:String,val canLie:Boolean,val power:Boolean,val toilet:Int?,val shower:Boolean,val verified:Boolean,val dayNoise:Int,val nightNoise:Int,val demoImage:Int,val reviews:List<Review>)

class MainActivity: ComponentActivity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{SleepGateApp()}} }

@Composable private fun SleepGateApp(){
 var selected by remember{mutableStateOf<Place?>(null)}; var night by remember{mutableStateOf(true)}; var userName by remember{mutableStateOf("Гость")}
 var showAuth by remember{mutableStateOf(false)}; var showAdd by remember{mutableStateOf(false)}; var showReview by remember{mutableStateOf(false)}
 var places by remember{mutableStateOf(seedPlaces())}; var selectedPhoto by remember{mutableStateOf<Uri?>(null)}
 var auth by remember{mutableStateOf<AuthResult?>(null)}; var cloudStatus by remember{mutableStateOf(if(SupabaseConfig.configured) "☁️ Cloud configured" else "📱 Demo mode")}; var busy by remember{mutableStateOf(false)}
 val scope=rememberCoroutineScope(); val context=androidx.compose.ui.platform.LocalContext.current; val client=remember{SupabaseClient()}
 val photoPicker=rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()){uri->selectedPhoto=uri}
 fun score(p:Place):Int{var s=70;if(p.canLie)s+=10;if(p.power)s+=5;if(p.toilet!=null&&p.toilet<=40)s+=4;if(p.shower)s+=3;s+=if(night)(5-p.nightNoise)*2 else (5-p.dayNoise);return s.coerceIn(0,100)}
 LaunchedEffect(SupabaseConfig.configured){ if(SupabaseConfig.configured){busy=true;runCatching{val c=client.fetchPlaces();if(c.isNotEmpty())places=c.map{cp->Place(cp.id,cp.title,cp.area,cp.canLie,cp.power,cp.toiletDistance,cp.shower,cp.verified,3,2,R.drawable.demo_gate_d,emptyList())};cloudStatus="☁️ Cloud synced (${places.size})"}.onFailure{cloudStatus="⚠️ Cloud error: ${it.message?.take(42)}"};busy=false} }
 LaunchedEffect(selectedPhoto, auth){ val a=auth; val photo=selectedPhoto; val p=selected; if(photo!=null&&a?.accessToken!=null&&a.userId!=null&&p!=null&&SupabaseConfig.configured){busy=true;runCatching{client.uploadPhoto(photo,context.contentResolver,p.id,a.userId,a.accessToken)}.onSuccess{cloudStatus=if(it)"☁️ Фото загружено" else "⚠️ Фото не загружено"}.onFailure{cloudStatus="⚠️ Фото: ${it.message?.take(45)}"};busy=false} }
 MaterialTheme{Scaffold(topBar={TopAppBar(title={Text("SleepGate • AUH")},actions={TextButton(onClick={night=!night}){Text(if(night)"🌙" else "☀️")};TextButton(onClick={showAuth=true}){Text(if(auth==null)"👤" else "✓")}})}){pad->
  if(selected==null){LazyColumn(Modifier.padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{Text("Zayed International Airport",style=MaterialTheme.typography.headlineSmall);Text("Terminal A • места для отдыха",color=MaterialTheme.colorScheme.onSurfaceVariant);Text(cloudStatus,color=MaterialTheme.colorScheme.primary);if(busy)LinearProgressIndicator(Modifier.fillMaxWidth())}
   item{Text(if(night)"Ночная оценка" else "Дневная оценка",color=MaterialTheme.colorScheme.primary)}
   items(places){p->Card(onClick={selected=p},modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Image(painterResource(p.demoImage),null,Modifier.fillMaxWidth().height(150.dp),contentScale=ContentScale.Crop);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(p.title,style=MaterialTheme.typography.titleLarge);AssistChip(onClick={},label={Text("${score(p)}")})};Text(p.area);Text((if(p.canLie)"🛏 Можно лечь  " else "🪑 Сидя  ")+(if(p.power)"🔌 Зарядка  " else "")+(p.toilet?.let{"🚻 ${it} м"}? : ""));Text(if(p.verified)"🟢 Подтверждено пользователем" else "🟡 Нужно проверить")}}}
   item{Button(onClick={showAdd=true},modifier=Modifier.fillMaxWidth()){Text("＋ Добавить место")}}
   item{Text("Фото в этой версии — демонстрационные иллюстрации и не являются фотографиями реального аэропорта.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
  }}else{val p=selected;LazyColumn(Modifier.padding(pad).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{TextButton(onClick={selected=null}){Text("← Назад")};Image(painterResource(p.demoImage),null,Modifier.fillMaxWidth().height(210.dp),contentScale=ContentScale.Crop);Text(p.title,style=MaterialTheme.typography.headlineMedium);Text(p.area)}
   item{Card{Column(Modifier.padding(18.dp)){Text("Sleep Score",style=MaterialTheme.typography.labelLarge);Text("${score(p)}/100",style=MaterialTheme.typography.displaySmall);Text(if(night)"Ночная оценка" else "Дневная оценка")}}}
   item{Text("Характеристики",style=MaterialTheme.typography.titleLarge)}; item{Text("🛏 Можно лечь: ${if(p.canLie)"да" else "нет"}\n🔌 Зарядка: ${if(p.power)"есть" else "нет"}\n🚻 Туалет: ${p.toilet?.let{"~$it м"}? : "не указано"}\n🚿 Душевые: ${if(p.shower)"есть в некоторых туалетах" else "нет данных"}\n🔊 Шум: ${if(night)p.nightNoise else p.dayNoise}/5")}
   item{Button(onClick={showReview=true},modifier=Modifier.fillMaxWidth()){Text("⭐ Оставить отзыв")}}
   item{Button(onClick={photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))},modifier=Modifier.fillMaxWidth()){Text("📸 Добавить фотографию")}}
   if(selectedPhoto!=null)item{Text("Фото выбрано. После авторизации его можно отправить в облако.",style=MaterialTheme.typography.bodySmall)}
   item{Text("Отзывы",style=MaterialTheme.typography.titleLarge)};items(p.reviews){r->Card{Column(Modifier.padding(14.dp)){Text("${r.author} • ${r.score}/5");Text(r.text)}}}
  }} }
  if(showAuth)AuthDialog(userName,auth!=null,{name,email,password,signUp->busy=true;scope.launch{runCatching{if(signUp)client.signUp(email,password) else client.signIn(email,password)}.onSuccess{a->auth=a;userName=name.ifBlank{"Гость"};cloudStatus="☁️ Вошёл в аккаунт"}.onFailure{cloudStatus="⚠️ ${it.message?.take(55)}"};busy=false;showAuth=false}}){showAuth=false}
  if(showAdd)AddDialog({showAdd=false}){title,area,lie,power,toilet,shower-> if(auth!=null&&SupabaseConfig.configured){busy=true;scope.launch{runCatching{client.fetchTerminalId()?.let{client.createPlace(NewCloudPlace(it,title,area,lie,power,toilet,shower),auth!!.accessToken!!)}};busy=false;showAdd=false}}else{places=places+Place("local-${System.currentTimeMillis()}",title,area,lie,power,toilet,shower,false,3,2,R.drawable.demo_gate_d,listOf());showAdd=false}}
  if(showReview&&selected!=null)ReviewDialog({showReview=false}){s,t->val id=selected!!.id;if(auth!=null&&SupabaseConfig.configured){busy=true;scope.launch{runCatching{client.addRating(id,auth!!.userId!!,auth!!.accessToken!!,s,t)};busy=false}};places=places.map{if(it.id==id)it.copy(reviews=it.reviews+Review(userName,s,t))else it};selected=places.first{it.id==id};showReview=false}
 }
}

@Composable private fun AuthDialog(current:String,logged:Boolean,onLogin:(String,String,String,Boolean)->Unit,onDismiss:()->Unit){var name by remember{mutableStateOf(current)};var email by remember{mutableStateOf("")};var pass by remember{mutableStateOf("")};var signUp by remember{mutableStateOf(true)};AlertDialog(onDismissRequest=onDismiss,title={Text(if(logged)"Профиль" else "SleepGate аккаунт")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){if(!logged){OutlinedTextField(name,{name=it},label={Text("Имя")});OutlinedTextField(email,{email=it},label={Text("Email")});OutlinedTextField(pass,{pass=it},label={Text("Пароль")});Row{Checkbox(signUp,{signUp=it});Text("Новый аккаунт")}}else Text("Авторизация активна.");Text("${if(SupabaseConfig.configured)"Облако подключено" else "Сначала настрой Supabase"}",color=MaterialTheme.colorScheme.onSurfaceVariant)}},confirmButton={TextButton(enabled=logged||email.contains("@")&&pass.length>=6,onClick={if(logged)onDismiss() else onLogin(name,email,pass,signUp)}){Text(if(logged)"Закрыть" else "Продолжить")}},dismissButton={if(!logged)TextButton(onClick=onDismiss){Text("Отмена")}})}
@Composable private fun AddDialog(onDismiss:()->Unit,onSave:(String,String,Boolean,Boolean,Int?,Boolean)->Unit){var title by remember{mutableStateOf("")};var area by remember{mutableStateOf("")};var lie by remember{mutableStateOf(true)};var power by remember{mutableStateOf(false)};var toilet by remember{mutableStateOf("")};var shower by remember{mutableStateOf(false)};AlertDialog(onDismissRequest=onDismiss,title={Text("Добавить место")},text={Column(verticalArrangement=Arrangement.spacedBy(6.dp)){OutlinedTextField(title,{title=it},label={Text("Название")});OutlinedTextField(area,{area=it},label={Text("Зона / gate")});Row{Checkbox(lie,{lie=it});Text("Можно лечь")};Row{Checkbox(power,{power=it});Text("Есть зарядка")};OutlinedTextField(toilet,{toilet=it.filter(Char::isDigit)},label={Text("Туалет, метров")});Row{Checkbox(shower,{shower=it});Text("Душевые")}},confirmButton={TextButton(enabled=title.isNotBlank(),onClick={onSave(title,area,lie,power,toilet.toIntOrNull(),shower)}){Text("Добавить")}},dismissButton={TextButton(onClick=onDismiss){Text("Отмена")}})}
@Composable private fun ReviewDialog(onDismiss:()->Unit,onSave:(Int,String)->Unit){var s by remember{mutableIntStateOf(5)};var t by remember{mutableStateOf("")};AlertDialog(onDismissRequest=onDismiss,title={Text("Ваш отзыв")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Оценка: $s/5");Row{(1..5).forEach{n->FilterChip(selected=s==n,onClick={s=n},label={Text("$n")})}};OutlinedTextField(t,{t=it},label={Text("Что важно знать другим?")},minLines=3)}},confirmButton={TextButton(onClick={onSave(s,t.ifBlank{"Без комментария"}))}{Text("Опубликовать")}},dismissButton={TextButton(onClick=onDismiss){Text("Отмена")}})}
private fun seedPlaces()=listOf(Place("auh-d-01","Диванчики у Gate D","Gate series D",true,true,35,true,true,3,1,R.drawable.demo_gate_d,listOf(Review("Евгений",5,"Удобные диванчики без подлокотников, рядом зарядки. Туалет примерно 30–40 м."))),Place("auh-fc-01","Широкие лавочки у Burger King","Food Court",true,false,25,true,true,3,2,R.drawable.demo_foodcourt,listOf(Review("Евгений",4,"Можно спать, персонал не выгоняет. Немного шумно, но терпимо."))))
