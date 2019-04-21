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

public class Main2Activity extends AppCompatActivity {
    EditText nombreD,edadD,generoD,experienciaD;
    Button insertarD,eliminarD;
    DatabaseReference servicioRealtime;
    ListView lista1;
    List<Director> datosConsultaDirectores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        nombreD = findViewById(R.id.nombreD);
        edadD = findViewById(R.id.edadD);
        generoD = findViewById(R.id.generoD);
        experienciaD = findViewById(R.id.añosD);
        insertarD = findViewById(R.id.insertarD);
        eliminarD = findViewById(R.id.eliminarD);
        servicioRealtime = FirebaseDatabase.getInstance().getReference();
        lista1 = findViewById(R.id.listaD);

        insertarD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarDirector();
            }
        });

        eliminarD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarDirector();
            }
        });
    }

    protected void onStart(){
        consultarD();
        super.onStart();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.opciones_p,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.adminPeliculas:
                Intent consultarP = new Intent(this,MainActivity.class);
                startActivity(consultarP);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void consultarD(){
        servicioRealtime.child("directores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                datosConsultaDirectores = new ArrayList<>();

                if(dataSnapshot.getChildrenCount()<=0){
                    Toast.makeText(Main2Activity.this,"No hay datos que mostrar",Toast.LENGTH_LONG).show();
                    return;
                }

                for(final DataSnapshot snap : dataSnapshot.getChildren()){
                    servicioRealtime.child("directores").child(snap.getKey()).addValueEventListener(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Director d = dataSnapshot.getValue(Director.class);

                                    if(d!=null){
                                        datosConsultaDirectores.add(d);
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
        if (datosConsultaDirectores.size()==0) {
            return;
        }
        String nombres[] = new String[datosConsultaDirectores.size()];

        for(int i = 0; i<nombres.length; i++){
            Director p = datosConsultaDirectores.get(i);
            nombres[i] = p.nombre;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres);
        lista1.setAdapter(adapter);

        lista1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(Main2Activity.this);
                final View directorD = getLayoutInflater().inflate(R.layout.director,null);
                Director d = datosConsultaDirectores.get(position);
                TextView D_nombre = directorD.findViewById(R.id.nombreDirector);
                TextView D_edad = directorD.findViewById(R.id.edadDirector);
                TextView D_genero = directorD.findViewById(R.id.generoDirector);
                TextView D_años = directorD.findViewById(R.id.experienciaDirector);

                D_nombre.setText(d.nombre);
                D_edad.setText(d.edad);
                D_genero.setText(d.generoPrincipal);
                D_años.setText(d.añosExperiencia);

                alerta.setTitle("INFORMACION").setMessage("Datos del director")
                        .setView(directorD)
                        .setPositiveButton("OK",null)
                        .show();
            }
        });
    }

    private void eliminarDirector(){
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        final EditText nombreD = new EditText(this);

        alerta.setTitle("ATENCION")
                .setMessage("Nombre del director a eliminar:")
                .setView(nombreD)
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(nombreD.getText().toString().isEmpty()){
                            Toast.makeText(Main2Activity.this, "El campo esta vacío",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        eliminarDirector1(nombreD.getText().toString());
                    }
                })
                .setNegativeButton("Cancelar",null)
                .show();
    }

    private void eliminarDirector1(String nombre){

        Query RazaQuery = servicioRealtime.child("directores").orderByChild("nombre").equalTo(nombre);
        RazaQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
                Toast.makeText(Main2Activity.this,"Se eliminó el director correctamente",Toast.LENGTH_LONG).show();
                consultarD();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void insertarDirector(){
        String nombre = nombreD.getText().toString();
        if(nombre.isEmpty()){
            Toast.makeText(Main2Activity.this,"Al menos introduzca el nombre del director a insertar",Toast.LENGTH_LONG).show();
            return;
        }
        Director peli = new Director(nombre,edadD.getText().toString(),generoD.getText().toString(),experienciaD.getText().toString());
        servicioRealtime.child("directores").push().setValue(peli).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Main2Activity.this,"Se insertó un nuevo director ",Toast.LENGTH_LONG).show();
                nombreD.setText("");
                edadD.setText("");
                generoD.setText("");
                experienciaD.setText("");
                consultarD();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Main2Activity.this,"Un error ocurrió al intentar insertar un nuevo director",Toast.LENGTH_LONG).show();
            }
        });
    }
}
