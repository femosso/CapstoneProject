package com.capstone.application.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.capstone.application.R;
import com.capstone.application.adapter.TeenListAdapter;
import com.capstone.application.model.Movie;
import com.capstone.application.model.Teen;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendsFragment extends Fragment {

    private static String TAG = "FriendsFragment";

    private Context mContext;

    private TeenListAdapter mAdapter;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_teens, container, false);


        listView = (ListView) rootView.findViewById(R.id.list);

        new RetrieveTeensTask(FriendsFragment.this).execute();

        // Code to Add an item with default animation
        //((TeenListAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((TeenListAdapter) mAdapter).deleteItem(index);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*private ArrayList<Teen> getTeens() {
        ArrayList results = new ArrayList<Teen>();
        for (int index = 0; index < 10; index++) {
            Teen obj = new Teen(index + "/5/1990", String.valueOf((index * 30) / 2),
                    new User("email", "firstname", "lastname"));
            results.add(index, obj);
        }
        return results;
    }*/

    private static final String url = "http://api.androidhive.info/json/movies.json";
    private ArrayList<Movie> movieList = new ArrayList<Movie>();

/*    private void fill() {

        // Creating volley request obj
        JsonArrayRequest movieReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Movie movie = new Movie();
                                movie.setTitle(obj.getString("title"));
                                movie.setThumbnailUrl(obj.getString("image"));
                                movie.setRating(((Number) obj.get("rating"))
                                        .doubleValue());
                                movie.setYear(obj.getInt("releaseYear"));

                                // Genre is json array
                                JSONArray genreArry = obj.getJSONArray("genre");
                                ArrayList<String> genre = new ArrayList<String>();
                                for (int j = 0; j < genreArry.length(); j++) {
                                    genre.add((String) genreArry.get(j));
                                }
                                movie.setGenre(genre);

                                // adding movie to movies array
                                movieList.add(movie);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        mAdapter = new TeenListAdapter(getActivity(), movieList);
                        listView.setAdapter(mAdapter);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(movieReq);
    }*/

    private class RetrieveTeensTask extends AsyncTask<Void, Void, List<User>> {

        private ProgressDialog dialog;

        public RetrieveTeensTask(FriendsFragment fragment) {
            dialog = new ProgressDialog(fragment.getActivity());
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Doing something, please wait.");
            dialog.show();
        }

        @Override
        protected List<User> doInBackground(Void... params) {
            Log.d(TAG, "Contacting server to retrieve list of teens");

            List<User> result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.SERVER_URL + "teen/list";

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to Teen object
                User[] users = restTemplate.getForObject(url, User[].class);

                if(users != null) {
                    result = Arrays.asList(users);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<User> result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            User user = new User();
            user.setFirstName("blablabla");
            user.setEmail("blablabla@gmail.com");

            Teen teen = new Teen();
            teen.setMedicalNumber("1234567");
            teen.setBirthday("22/05/1990");
            user.setTeen(teen);

            List<User> list = new ArrayList<>();
            list.add(user);
            result = list;

            mAdapter = new TeenListAdapter(getActivity(), result);
            listView.setAdapter(mAdapter);
        }
    }
}