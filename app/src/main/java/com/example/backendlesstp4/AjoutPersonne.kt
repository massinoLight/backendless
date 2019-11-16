package com.example.backendlesstp4


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.personne_ajout.*
import android.widget.LinearLayout as LinearLayout1


class AjoutPersonne : AppCompatActivity() {

    companion object {
        const val EXTRA_VALIDER = "AjouterPersonne.valider"
        const val EXTRA_NOM = "AjouterPersonne.nom"
        const val EXTRA_EMAIL = "AjouterPersonne.email"
        const val EXTRA_TEL = "AjouterPersonne.tel"
        const val EXTRA_FAXE = "AjouterPersonne.fixe"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.personne_ajout)

        //le bouton ajouter on recup les informations saisies et on les passe en paaramétre a l'intent
        bt_ajouter.setOnClickListener {
            if ((ed_nom.text.toString()=="")&&(ed_tel.text.toString()=="")){
                Toast.makeText(this, "vous devez saisir au moins un nom et un numero de telephone", Toast.LENGTH_LONG).show()

            }
            else{
                val intent = Intent()
                // On a valider (=> true)
                intent.putExtra(EXTRA_VALIDER, true)
                // On donne la valeur tapée en retour
                intent.putExtra(EXTRA_NOM,ed_nom.text.toString() )
                intent.putExtra(EXTRA_EMAIL,ed_email.text.toString() )
                intent.putExtra(EXTRA_TEL,ed_tel.text.toString() )
                intent.putExtra(EXTRA_FAXE,ed_fixe.text.toString() )
                setResult(RESULT_OK, intent)
                finish()
            }

        }

        //mettre les champs a vide
        bt_annuler.setOnClickListener {

            ed_nom.setText("")
            ed_email.setText("")
            ed_tel.setText("")
            ed_fixe.setText("")
        }


    }
}
