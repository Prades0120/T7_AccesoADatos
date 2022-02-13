import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JComboBox
import javax.swing.JTextArea
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel
import java.awt.Color
import javax.swing.JScrollPane
import java.io.FileInputStream
import com.google.firebase.FirebaseOptions
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.DocumentChange
import com.google.firebase.FirebaseApp
import com.google.firebase.cloud.FirestoreClient
import java.awt.EventQueue

class EstadisticaCF : JFrame() {

    private val etCombo = JLabel("Llista de províncies:")
    private val comboProv = JComboBox<String>()

    private val etiqueta = JLabel("Estadístiques:")
    private val area = JTextArea()

    // en iniciar posem un contenidor per als elements anteriors
    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setBounds(100, 100, 450, 400)
        layout = BorderLayout()
        // contenidor per als elements

        val panell1 = JPanel(FlowLayout())
        panell1.add(etCombo)
        panell1.add(comboProv)
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
            .build()

        FirebaseApp.initializeApp(options)

        val db = FirestoreClient.getFirestore()

        // Instruccions per a omplir el JComboBox amb les províncies
        val mutable = mutableSetOf<String>()


        db.collection("Estadistica").orderBy("Provincia").addSnapshotListener { snapshots, e ->
            if (e != null) {
                System.err.println("Listen failed: $e")
                return@addSnapshotListener
            }

            for (dc in snapshots!!.documentChanges) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        val nomP = dc.document.getString("Provincia")
                        mutable.add(nomP.toString())

                    }
                    DocumentChange.Type.MODIFIED ->
                        println("Missatge modificat: " + dc.document.data)

                    DocumentChange.Type.REMOVED ->
                        println("Missatge esborrat: " + dc.document.data)
                }
            }
            mutable.forEach {
                comboProv.addItem(it)
            }
        }
        // Instruccions per agafar la informació de tots els anys de la província triada
        comboProv.addActionListener {
            area.text=""
            db.collection("Estadistica").orderBy("any").whereEqualTo("Provincia", comboProv.selectedItem).addSnapshotListener { snapshots, e ->
                if (e != null) {
                    System.err.println("Listen failed: $e")
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            area.append("" + dc.document.getString("any") + ": " + dc.document.getString("Dones") + " - " + dc.document.getString("Homes") + "\n")
                        }
                        DocumentChange.Type.MODIFIED ->
                            println("Missatge modificat: " + dc.document.data)

                        DocumentChange.Type.REMOVED ->
                            println("Missatge esborrat: " + dc.document.data)
                    }
                }
            }
        }
    }
}
fun main() {
    EventQueue.invokeLater {
        EstadisticaCF().isVisible = true
    }
}