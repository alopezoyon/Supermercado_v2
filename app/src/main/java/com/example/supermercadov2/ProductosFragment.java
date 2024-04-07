package com.example.supermercadov2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import java.util.ArrayList;
import java.util.List;

//Esta clase es el fragment utilizado en el caso de haber puesto el móvil en horizontal en la pantalla de "MenuPrincipal"
public class ProductosFragment extends ListFragment {
    private DatabaseHelper databaseHelper;
    private List<Producto> listaProductos;
    private listenerDelFragment elListener;

    public ProductosFragment() {
    }

    public interface listenerDelFragment {
        void seleccionarElemento(String elemento);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
    }


    //En este método se cargan los productos del supermercado
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseHelper = new DatabaseHelper(requireContext());

        Bundle args = getArguments();

        if (args != null) {
            String nombreSupermercado = args.getString("nombreSupermercado");

            if (nombreSupermercado != null) {
                //cargarProductos(nombreSupermercado);
            }
        }
    }

    //En este método se carga el view
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productos, container, false);
        listaProductos = new ArrayList<>();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            elListener = (listenerDelFragment) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " debe implementar listenerDelFragment");
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String elemento = listaProductos.get(position).toString();
        elListener.seleccionarElemento(elemento);
    }

    /*
    //Método para cargar los productos de un supemercado determinado
    public void cargarProductos(String nombreSupermercado) {
        listaProductos.clear();
        listaProductos.addAll(databaseHelper.getProductosPorSupermercado(nombreSupermercado));

        ArrayAdapter<Producto> adapter = new ArrayAdapter<Producto>(
                requireContext(),
                android.R.layout.simple_list_item_2,
                android.R.id.text1,
                listaProductos
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Producto producto = (Producto) getItem(position);
                if (producto != null) {
                    String displayText = producto.getNombre() + " " + producto.getPrecio() + "€";
                    ((android.widget.TextView) view.findViewById(android.R.id.text1)).setText(displayText);
                }

                return view;
            }
        };

        setListAdapter(adapter);
    }

     */
}