package com.example.exereader.ui.opciones;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.exereader.BuildConfig;
import com.example.exereader.MainActivity;
import com.example.exereader.R;
import com.example.exereader.ClaseSharedPreferences;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.Locale;

/** OPCIONES */
public class OpcionesFragment extends Fragment implements View.OnClickListener {
    private Button borrarApp, wha, tele, compar;
    private TextView nombre;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_opciones, container, false);

        nombre=root.findViewById(R.id.nombre);
        borrarApp=root.findViewById(R.id.borrarApp);
        wha=root.findViewById(R.id.wha);
        tele=root.findViewById(R.id.te);
        compar =root.findViewById(R.id.face);

        //Bloqueamos las opciones de sobreProyecto
        setHasOptionsMenu(true);

        borrarApp.setOnClickListener(this);
        wha.setOnClickListener(this);
        tele.setOnClickListener(this);
        compar.setOnClickListener(this);

        nombre.setText(ClaseSharedPreferences.verDatos(getContext(),"tp"));

        return root;
    }

    /** Controlamos en que botón pulsa el usuario para realizar una opción u otra */
    @Override
    public void onClick(View v) {
        String idioma =  Locale.getDefault().getLanguage(); // es
        Uri uri = FileProvider.getUriForFile(getContext(), "com.example.exereader.fileprovider", new File(ClaseSharedPreferences.verDatos(getContext(), "archivo")+".zip"));
        switch (v.getId()){
            case R.id.wha:
                Intent compartir = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                compartir.setAction(Intent.ACTION_SEND);
                compartir.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                compartir.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                compartir.putExtra(Intent.EXTRA_STREAM,uri);
                compartir.setType("application/zip");
                compartir.setPackage("com.whatsapp");
                try {
                    getActivity().startActivity(compartir);
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                    if(!idioma.equalsIgnoreCase("es")){
                        Snackbar.make(getView(), "Error - Application not installed.", Snackbar.LENGTH_SHORT)
                                .show();
                    }else{
                        Snackbar.make(getView(), "Error - Aplicación no instalada.", Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }
                break;
            case R.id.te:
                Intent compartirTe = new Intent();
                compartirTe.setAction(Intent.ACTION_SEND);
                compartirTe.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                compartirTe.putExtra(Intent.EXTRA_STREAM,uri);
                compartirTe.setType("application/zip");
                compartirTe.setPackage("org.telegram.messenger");
                try {
                    getActivity().startActivity(compartirTe);
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                    if(!idioma.equalsIgnoreCase("es")){
                        Snackbar.make(getView(), "Error - Application not installed.", Snackbar.LENGTH_SHORT)
                                .show();
                    }else{
                        Snackbar.make(getView(), "Error - Aplicación no instalada.", Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }
                break;
            case R.id.face:
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("application/zip");
                if(!idioma.equalsIgnoreCase("es")){
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Project sent from eXeReader");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    getActivity().startActivity(Intent.createChooser(intent, "Share using..."));
                }else{
                    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Proyecto enviado desde eXeReader");//se usará por ejemplo para email
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    getActivity().startActivity(Intent.createChooser(intent, "Compartir usando..."));
                }

                break;
            case R.id.borrarApp:
                File f= new File(ClaseSharedPreferences.verDatos(getContext(), "directorio"));
                //Pedir confirmación antes de eliminar un proyecto.
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                if(!idioma.equalsIgnoreCase("es")){
                    builder.setTitle("Confirmation");
                    builder.setMessage("Delete the contents of the application. Continue?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        borrarApp(f);
                        ClaseSharedPreferences.eliminarDatos(getContext(), "cambio");
                        ClaseSharedPreferences.guardarDatos(getContext(),"cambio","no");
                        ((MainActivity) getActivity()).activarMenu();
                    });
                }else{
                    builder.setTitle("Confirmación");
                    builder.setMessage("Eliminar el contenido de la aplicación. ¿Continuar?");
                    //Si el usuario pulsa en si, procedemos a borrar el contenido.
                    builder.setPositiveButton("Sí", (dialog, which) -> {
                        borrarApp(f);
                        ClaseSharedPreferences.eliminarDatos(getContext(), "cambio");
                        ClaseSharedPreferences.guardarDatos(getContext(),"cambio","no");
                        ((MainActivity) getActivity()).activarMenu();
                    });
                }
                builder.setNegativeButton("No", (dialog, which) -> {
                });
                builder.show();
                break;
        }
    }

    /** Métodos usados para borrar el proyecto de la aplicación */
    private void borrarApp(File f) {
        String idioma =  Locale.getDefault().getLanguage(); // es
        String path = getContext().getExternalFilesDir(null).toString();
        File carpetaFicheros = new File(path);
        File[] files = carpetaFicheros.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equalsIgnoreCase(f.getName()) || files[i].getName().equalsIgnoreCase(f.getName() + ".zip")) {
                if(files[i].isDirectory()){
                    borrarDirectorio(files[i]);
                }
                files[i].delete();
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if(!idioma.equalsIgnoreCase("es")){
            builder.setMessage("File successfully deleted.").setTitle("Deleted")
                    .setPositiveButton("Ok", (dialogInterface, which) -> dialogInterface.cancel());
        }else {
            builder.setMessage("Archivo eliminado correctamente.").setTitle("Eliminado")
                    .setPositiveButton("Ok", (dialogInterface, which) -> dialogInterface.cancel());
        }
        builder.show();
    }

    private void borrarDirectorio(File f) {
        File[] archivos = f.listFiles();
        for (int i = 0; i < archivos.length; i++) {
            if (archivos[i].isDirectory()) {
                borrarDirectorio(archivos[i]);
            } else {
                archivos[i].delete();
            }
        }
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.findItem(R.id.ordenar).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }
}