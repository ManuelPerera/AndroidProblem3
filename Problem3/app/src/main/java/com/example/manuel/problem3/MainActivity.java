package com.example.manuel.problem3;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtilsHC4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Main activity. Inicializa los elementos de la vista y las variables globales.
 */
public class MainActivity extends Activity {

    final ArrayList<String> list = new ArrayList<>();
    StableArrayAdapter adapter;

    /**
     * Crea la vista e inicializa sus elementos. Al terminar llama a la operación encargada de
     * descargar el código html y de actualizar la lista a mostrar.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView listview = (ListView) findViewById(R.id.listView1);
        adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /**
             * Típica operación para determinar el comportamiento de la lista al hacer click. En
             * este caso no hace nada.
             * @param parent
             * @param view
             * @param pos
             * @param id
             */
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int pos, long id) {

            }
        });

        String[] params = new String[1];
        params[0] = "http://www.visual-engin.com/Web/";
        new LoadHTMLTask().execute(params);
    }

    /**
     * Craga el código html en segundo plano y lo parsea para identificar y guardar los enlaces de
     * la página
     */
    private class LoadHTMLTask extends AsyncTask<String, Void, String> {

        /**
         * Recibe el código html de la página, lo parsea e identifica los enlaces a mostrar en la
         * lista. Según encuentra los enlaces los añade a la lista de elementos a mostrar. Si el
         * enlace ya existe en la lista, no lo añade. Una vez ha parseado el código html, notifica a
         * la lista que se ha actualizado su contenido. Si no se ha recibido el código html,
         * consider que ha sido un error de conexión y genera un toast de error.
         * @param result
         */
        protected void onPostExecute(String result) {
            if (result != null) {

                String[] tokens = result.split("[ ]+");
                for (int i = 0; i < tokens.length; i++) {
                    if(tokens[i].contains("<a")) {
                        int j = i+1;
                        for (; j < tokens.length && !tokens[j].startsWith("href=\"") &&
                                !tokens[j].contains("</a>"); j++){}
                        i = j;
                        if(!tokens[j].contains("</a>")) {
                            String newurl = tokens[i].split("[\"]")[1];
                            if (!list.contains(newurl))list.add(newurl);
                        }
                    }
                }
                adapter.notifyDataSetChanged();

            } else {
                Toast.makeText(getApplicationContext(),
                        "Hay problemas de conexion", Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Descarga en segundo plano, para no bloquear la aplicación, la página indicada por param[0]
         * y guarda el código html en un string.
         * @param param
         * @return Un string con el código html de la página indicada en param[0] o null si hay algún
         * error.
         */
        @Override
        protected String doInBackground(String... param) {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpGetHC4 get = new HttpGetHC4(param[0]);
            CloseableHttpResponse response = null;
            try {
                response = httpClient.execute(get);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String bodyHtml = null;
            try {
                bodyHtml = EntityUtilsHC4.toString(response.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bodyHtml;
        }
    }

    /**
     * Típica función por default para generar un menu en la vista.
     * @param menu
     * @return siempre ture
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Adaptador para generar una lista custom de elementos. Dando el contexto, el textview y una
     * lista de elementos genera un hashmap que nos sirve, una vez asignado, para representar los
     * elementos en la listview.
     */
    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        /**
         * Crea el hashmap en donde guardaremos los elementos a mostrar en la lista.
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

    }
}
