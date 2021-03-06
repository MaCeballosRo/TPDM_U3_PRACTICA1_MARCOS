package mx.edu.ittepic.marcos.tpdm_u3_practica1_marcos;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
        lista = findViewById(R.id.lista);

        insertarP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarP();
            }
        });

        eliminarP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarPeli();
            }
        });
    }

    protected void onStart(){
        consultarP();
        super.onStart();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.opciones_d,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.adminDirectores:
                Intent consultarP = new Intent(this,Main2Activity.class);
                startActivity(consultarP);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void consultarP(){
        servicioRealtime.child("peliculas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                datosConsultaPelicula = new ArrayList<>();

                if(dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(MainActivity.this,"No hay datos que mostrar",Toast.LENGTH_LONG).show();
                    return;
                }

                for(final DataSnapshot snap : dataSnapshot.getChildren()){
                    servicioRealtime.child("peliculas").child(snap.getKey()).addValueEventListener(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Pelicula u = dataSnapshot.getValue(Pelicula.class);

                                    if(u!=null){
                                        datosConsultaPelicula.add(u);
                                    }
                                    cargarSelect();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            }
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void cargarSelect(){
        if (datosConsultaPelicula.size()==0) return;
        String nombres[] = new String[datosConsultaPelicula.size()];

        for(int i = 0; i<nombres.length; i++){
            Pelicula u = datosConsultaPelicula.get(i);
            nombres[i] = u.titulo+"  -  "+u.año;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres);
        lista.setAdapter(adapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
                final View peliculaD = getLayoutInflater().inflate(R.layout.pelicula,null);
                Pelicula p = datosConsultaPelicula.get(position);
                TextView P_titulo = peliculaD.findViewById(R.id.tituloPelicula);
                TextView P_año = peliculaD.findViewById(R.id.añoPelicula);
                TextView P_genero = peliculaD.findViewById(R.id.generoPelicula);
                TextView P_director = peliculaD.findViewById(R.id.directorPelicula);

                P_titulo.setText(p.titulo);
                P_año.setText(p.año);
                P_genero.setText(p.genero);
                P_director.setText(p.director);

                alerta.setTitle("INFORMACION").setMessage("Datos de película")
                        .setView(peliculaD)
                        .setPositiveButton("OK",null)
                        .show();
            }
        });
    }

    private void eliminarPeli(){
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        final EditText tituloE = new EditText(this);
        tituloE.setHint("No debe quedar vacío");

        alerta.setTitle("ATENCION")
                .setMessage("Nombre de película a borrar:")
                .setView(tituloE)
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(tituloE.getText().toString().isEmpty()){
                            Toast.makeText(MainActivity.this, "El campo esta vacío",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        eliminarPelicula(tituloE.getText().toString());
                    }
                })
                .setNegativeButton("Cancelar",null)
                .show();
    }

    private void eliminarPelicula(String titulo){

        Query RazaQuery = servicioRealtime.child("peliculas").orderByChild("titulo").equalTo(titulo);
        RazaQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
                Toast.makeText(MainActivity.this,"Se eliminó la película correctamente",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void insertarP(){
        String titulo = tituloP.getText().toString();
        if(titulo.isEmpty()){
            Toast.makeText(MainActivity.this,"Al menos introduzca el título de la película a insertar",Toast.LENGTH_LONG).show();
            return;
        }
        Pelicula peli = new Pelicula(titulo,añoP.getText().toString(),generoP.getText().toString(),directorP.getText().toString());
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
