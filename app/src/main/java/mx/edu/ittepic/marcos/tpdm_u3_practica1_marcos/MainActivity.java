package mx.edu.ittepic.marcos.tpdm_u3_practica1_marcos;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText tituloP, añoP, generoP,directorP;
    Button insertarP,eliminarP;
    DatabaseReference servicioRealtime;
    ListView lista;
    List<Pelicula> datosConsultaPelicula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tituloP = findViewById(R.id.tituloP);
        añoP = findViewById(R.id.añoP);
        generoP = findViewById(R.id.generoP);
        directorP = findViewById(R.id.directorP);
        insertarP = findViewById(R.id.insertarP);
        eliminarP = findViewById(R.id.eliminarP);
        servicioRealtime = FirebaseDatabase.getInstance().getReference();

        insertarP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarP();
            }
        });
    }

    protected void onStart(){
        consultarP();
        super.onStart();
    }



    private void consultarP(){
        servicioRealtime.child("peliculas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                datosConsultaPelicula = new ArrayList<>();

                servicioRealtime.child("peliculas").child(dataSnapshot.getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snap: dataSnapshot.getChildren()){
                            Pelicula pelicula = snap.getValue(Pelicula.class);

                            if(pelicula!=null){
                                datosConsultaPelicula.add(pelicula);
                            }
                        }
                        crearListView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void crearListView(){
        if(datosConsultaPelicula.size()<=0){
            return;
        }

        String[] nombres = new String[datosConsultaPelicula.size()];
        for(int i=0;i<nombres.length;i++){
            Pelicula j = datosConsultaPelicula.get(i);
            nombres[i] = j.titulo;
        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,nombres);

        lista.setAdapter(adaptador);
    }

    private void eliminarPelicula(){
        Query RazaQuery = servicioRealtime.child("peliculas").orderByChild("").equalTo("");
        RazaQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void insertarP(){
        Pelicula peli = new Pelicula(tituloP.getText().toString(),añoP.getText().toString(),generoP.getText().toString(),directorP.getText().toString());
        servicioRealtime.child("peliculas").push().setValue(peli).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this,"Se insertó la película correctamente",Toast.LENGTH_LONG).show();
                consultarP();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Un error ocurrió al intentar registrar la película",Toast.LENGTH_LONG).show();
            }
        });
    }
}
