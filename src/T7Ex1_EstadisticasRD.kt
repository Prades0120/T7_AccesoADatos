import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JComboBox
import javax.swing.JTextArea
import java.awt.BorderLayout
import javax.swing.JPanel
import java.awt.FlowLayout
import java.awt.Color
import javax.swing.JScrollPane
import java.io.FileInputStream
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ChildEventListener
import java.awt.EventQueue


class EstadisticaRD : JFrame() {

    val etProv = JLabel("Provincia: ")
    val provincia = JComboBox<String>()

    val etiqueta = JLabel("Missatges:")
    val area = JTextArea()


    // en iniciar posem un contenidor per als elements anteriors
    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setBounds(100, 100, 450, 450)
        layout = BorderLayout()
        // contenidor per als elements
        //Hi haurà títol. Panell de dalt: últim missatge. Panell de baix: per a introduir missatge. Panell central: tot el xat

        val panell1 = JPanel(FlowLayout())
        panell1.add(etProv)
        panell1.add(provincia)
        contentPane.add(panell1, BorderLayout.NORTH)

        val panell2 = JPanel(BorderLayout())
        panell2.add(etiqueta, BorderLayout.NORTH)
        area.foreground = Color.blue
        area.isEditable = false
        val scroll = JScrollPane(area)
        panell2.add(scroll, BorderLayout.CENTER)
        contentPane.add(panell2, BorderLayout.CENTER)

        isVisible = true

        val serviceAccount = FileInputStream("xat-ad-firebase-adminsdk-my2d0-8c69944b34.json")

        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://xat-ad.firebaseio.com").build()

        FirebaseApp.initializeApp(options)

        // Posar tota la llista de províncies al JComboBox anomenat provincia
        val database = FirebaseDatabase.getInstance()
        val ref1 = database.getReference("EstadisticaVariacioPoblacional")

        ref1.addChildEventListener(object : ChildEventListener {
            override
            fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                provincia.addItem((dataSnapshot.child("nombre").value.toString()))
            }

            override
            fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            }

            override
            fun onChildRemoved(dataSnapshot: DataSnapshot) {
            }

            override
            fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
            }

            override
            fun onCancelled(databaseError: DatabaseError) {
            }
        }
        )




        provincia.addActionListener {
            // Posar la informació de tots els anys en el JTextArea anomenat area
            area.text=""
            val posicion = provincia.selectedIndex
            val ref2 = database.getReference("EstadisticaVariacioPoblacional/${posicion}/data")
            ref2.addChildEventListener(object : ChildEventListener {
                override
                fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    area.append(dataSnapshot.child("nombrePeriodo").value.toString() + ": "
                            + dataSnapshot.child("valor").value.toString() + "\n"
                    )
                }

                override
                fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                }

                override
                fun onChildRemoved(dataSnapshot: DataSnapshot) {
                }

                override
                fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                }

                override
                fun onCancelled(databaseError: DatabaseError) {
                }
            }
            )

        }
    }
}

fun main() {
    EventQueue.invokeLater {
        EstadisticaRD().isVisible = true
    }
}

