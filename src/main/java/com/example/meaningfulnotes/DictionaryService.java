package com.example.meaningfulnotes;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DictionaryService {
    @GET("api/v2/entries/en/{word}")
    Call<List<DictionaryResponse>> getDictionaryData(@Path("word") String word);
}

