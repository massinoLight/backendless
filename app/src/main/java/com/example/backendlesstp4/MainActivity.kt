package com.example.backendlesstp4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Handler
import android.os.Message

import com.backendless.Backendless
import com.backendless.persistence.DataQueryBuilder
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult

import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import android.net.ConnectivityManager as ConnectivityManager1


data class PersonneBackendLess(var objectId: String? = null,
                               var Bnom: String = "",
                               var Bemail:String="",
                               var Btel:String="",
                               var Bfixe:String="")


class MainActivity : AppCompatActivity() {


    companion object{
        val APP_ID="0FA7A3A1-FEB8-18A4-FFF6-D3C5BAB04D00"
        val API_KEY="800ADCBB-7B28-8245-FF30-3F3B0373AF00"
    }

    var personnes = mutableListOf<Personne>()

    var i=0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //la ligne a ajouter pour indiquer l'app id et app key
        Backendless.initApp(this, APP_ID, API_KEY)

/**
 *dn va faire appel a notre class intern inner Recuperation
 * qui va executer une tache Asyc
 *
 * */
        val rec=Recuperation()

        /***
         * verifier si il y a accée a internet si oui
         * faire la récupération de backendless
         * sinn dire a l'utilisateur qu'il n'a pas accé a internet
         *
         * **/
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager1
        when(connMgr.activeNetworkInfo?.type){
            ConnectivityManager1.TYPE_WIFI, ConnectivityManager1.TYPE_MOBILE ->
                rec.execute("stp")
            null -> { toast("Pas de réseau") }
        }




        buildRecyclerView()

        //le bouton pour permettre la saisie d'un contact
        btn_ajouter.setOnClickListener {

            startActivityForResult<AjoutPersonne>(1)


        }
    }

    /***
     *
     * la classe intern qui execute des threads en taches de fond
     *
     * ***/
    inner class Recuperation(): AsyncTask<String, Int, MutableList<MutableMap<Any?, Any?>>>() {

        lateinit var mcontext :Context

        /****
         *On verifie qu'il y a bien accé a Internet
         * avant de lancer la recupération de Backendless
         *
         * ****/
        /*fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager1
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }*/

        override fun onPreExecute() {
            super.onPreExecute()


        }

        /***
         *
         * Fonction qui va s'executer en tache de fond pour recupérer les informations du cloud
         * a la fin de son traitement elle va les transmette a la  fonction onPostExecute
         * qui est chargé de l'affichage
         *
         * **/
        override fun doInBackground(vararg params: String?): MutableList<MutableMap<Any?, Any?>>? {


            //si on souhaite avoir le nombre de contacts qui sont  stocké dans le cloud
            //val count =Backendless.Data.of( "PersonneBackendLess" ).getObjectCount()

                val lesContacts = Backendless.Data.of("PersonneBackendLess")
                    .find(DataQueryBuilder.create().setPageSize(25).setOffset(0))


                /*Backendless.Persistence
                .of(Message::class.java)
                .objectCount*/
                return lesContacts



        }


        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
        }

        /**
         * Cette fonction va s'executer apres le doInBackground qui est une tache Asy
         * elle va recup le resultat emis par cette dérniére et va les
         * placer dans notre liste et les afficher sur notre recyclerView
         *
         * **/
        override fun onPostExecute(result: MutableList<MutableMap<Any?, Any?>>) {
            super.onPostExecute(result)
            for (msg in result){

                val p10=Personne("${msg["bnom"]}","${msg["bemai"]}","${msg["btel"]}","${msg["bfixe"]}")
                personnes.add(p10)
                personnes.sortWith(compareBy({it.nom}))
                buildRecyclerView()
                mon_recycler.adapter?.notifyItemInserted(0)

            }

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            1 -> {
                // Résultat de startActivityForResult<ModifierActivity>
                if(resultCode == Activity.RESULT_OK){
                    val valider = data?.getBooleanExtra(AjoutPersonne.EXTRA_VALIDER, false) ?: false
                    if(valider){
                        // L'utilisateur a utilisé le bouton "valider"
                        // On récupère la valeur dans l'extra (avec une valeur par défaut de "")
                        val nouvValeurnom = data?.getStringExtra(AjoutPersonne.EXTRA_NOM) ?: ""
                        val nouvValeuremail = data?.getStringExtra(AjoutPersonne.EXTRA_EMAIL) ?: ""
                        val nouvValeurtel = data?.getStringExtra(AjoutPersonne.EXTRA_TEL) ?: ""
                        val nouvValeurfixe = data?.getStringExtra(AjoutPersonne.EXTRA_FAXE) ?: ""

                        var p8= Personne(nouvValeurnom,nouvValeuremail,nouvValeurtel,nouvValeurfixe)

                        /******
                         *
                         * la partie stockage cloud
                         *
                         * **/

                        //un objet PersonneBackendLess que l on va stocker dans notre cloud
                        val per = PersonneBackendLess(null, nouvValeurnom, nouvValeuremail,
                            nouvValeurtel,nouvValeurfixe)

                        doAsync {
                            Backendless.Persistence
                                .of(PersonneBackendLess::class.java).save(per)
                        }



                        toast("Données bien ajouté au cloud")
                        personnes.add(0,p8)
                        //cette ligne permet de trier la liste des contactes par ordre alphabetique
                        personnes.sortWith(compareBy({it.nom}))
                        buildRecyclerView()
                        mon_recycler.adapter?.notifyItemInserted(0)

                    }else{
                        //ID--
                    }
                }else if(resultCode == Activity.RESULT_CANCELED){
                    // L'utilisateur a utilisé le bouton retour <- de son téléphone
                    // on ne fait rien de spécial non plus
                }
            }
        }
    }


    fun buildRecyclerView() {
        mon_recycler.setHasFixedSize(true)
        //mon_recycler.setAdapter(mAdapter)
        mon_recycler.layoutManager = LinearLayoutManager(this)

        mon_recycler.adapter = PersonneAdapter(personnes.toTypedArray())
        {
            //ici on affiche juste toutes les informations dans un Toast
            //on aurait tres bien pu les passer en parametre avec un intent et les afficher dans une autre activity
            Toast.makeText(this, "Element selectionné: ${it}", Toast.LENGTH_LONG).show()
            var  nom="${it.nom}"
            var  tel="${it.tel}"
            var  mail="${it.email}"
            var  faxe="${it.fixe}"
            val intent3 = Intent(this, AfficheDetailActivity::class.java)
            intent3.putExtra("NOM",nom)
            intent3.putExtra("TEL",tel)
            intent3.putExtra("MAIL",mail)
            intent3.putExtra("FAXE",faxe)
            startActivity(intent3)


        }


    }



}