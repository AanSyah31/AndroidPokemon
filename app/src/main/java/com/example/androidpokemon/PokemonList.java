package com.example.androidpokemon;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.androidpokemon.Adapter.PokemonListAdapter;
import com.example.androidpokemon.Common.Common;
import com.example.androidpokemon.Common.ItemOffsetDecoration;
import com.example.androidpokemon.Model.Pokedex;
import com.example.androidpokemon.Model.Pokemon;
import com.example.androidpokemon.Retrofit.IPokemonDex;
import com.example.androidpokemon.Retrofit.RetrofitClient;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class PokemonList extends Fragment {


    IPokemonDex iPokemonDex;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    RecyclerView pokemon_list_recyclerview;

    PokemonListAdapter adapter,search_adapter;
    List<String> last_suggest = new ArrayList<>();

    MaterialSearchBar searchBar;


    static PokemonList instance;

    public static PokemonList getInstance(){
        if (instance == null)
            instance = new PokemonList();
        return instance;

    }


    public PokemonList() {
        Retrofit retrofit = RetrofitClient.getInstace();
        iPokemonDex = retrofit.create(IPokemonDex.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pokemon_list, container, false);

        pokemon_list_recyclerview = (RecyclerView) view.findViewById(R.id.pokemon_list_recyclerview);
        pokemon_list_recyclerview.setHasFixedSize(true);
        pokemon_list_recyclerview.setLayoutManager(new GridLayoutManager(getActivity(),2));
        ItemOffsetDecoration itemOffsetDecoration = new ItemOffsetDecoration(getActivity(),R.dimen.spacing);
        pokemon_list_recyclerview.addItemDecoration(itemOffsetDecoration);

        //Setup SearchBar
        searchBar = (MaterialSearchBar)view.findViewById(R.id.search_bar);
        searchBar.setHint("Enter Pokemon name");
        searchBar.setCardViewElevation(10);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    List<String> suggest = new ArrayList<>();
                    for (String search:last_suggest)
                    {
                        if (search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                            suggest.add(search);
                    }
                    searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (enabled)
                    pokemon_list_recyclerview.setAdapter(adapter); //Return default adapter
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSeach(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });



        fetchData();

        return view;
    }

    private void startSeach(CharSequence text) {
        if (Common.commonPokemonList.size() > 0)
        {
            List<Pokemon> result = new ArrayList<>();
            for (Pokemon pokemon:Common.commonPokemonList)
                if (pokemon.getName().toLowerCase().contains(text.toString().toLowerCase()))
                    result.add(pokemon);
                search_adapter = new PokemonListAdapter(getActivity(),result);
                pokemon_list_recyclerview.setAdapter(search_adapter);
        }
    }

    private void fetchData() {
        compositeDisposable.add(iPokemonDex.getListPokemon()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Pokedex>() {
                    @Override
                    public void accept(Pokedex pokedex) throws Exception {
                        Common.commonPokemonList = pokedex.getPokemon();
                        adapter = new PokemonListAdapter(getActivity(),Common.commonPokemonList);
                        last_suggest.clear();
                        pokemon_list_recyclerview.setAdapter(adapter);

                        for (Pokemon pokemon:Common.commonPokemonList)
                            last_suggest.add(pokemon.getName());
                        searchBar.setVisibility(View.VISIBLE); //Display search bar after load  all pokemon from DB
                        searchBar.setLastSuggestions(last_suggest);
                    }
                })
        );
    }

}
