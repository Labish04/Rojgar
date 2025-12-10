package com.example.rojgar.viewmodel

import androidx.lifecycle.ViewModel
import com.example.rojgar.model.UserModel
import com.example.rojgar.repository.UserRepo
import com.google.firebase.auth.FirebaseUser

class UserViewModel (val  repo: UserRepo): ViewModel() {

    fun login(
        email: String, password: String,
        callback: (Boolean, String) -> Unit)
    {
        repo.login( email,password,callback)

    }

    fun register(
        email: String, password: String,
        callback: (Boolean, String, String) -> Unit)
    {
        repo.register(email,password,callback)

    }

    fun addUserToDatabase(
        userId: String,
        model: UserModel, callback: (Boolean, String) -> Unit)
    {
       repo.addUserToDatabase(userId,model,callback)
    }
    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)
    {
        repo.forgetPassword(email,callback)

    }
    fun editProfile(
        userId: String, model: UserModel,
        callback: (Boolean, String) -> Unit)
    {
      repo.editProfile(userId,model,callback)
    }

    fun logout(callback: (Boolean, String) -> Unit)
    {
       repo.logout(callback)

    }

    fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit)
    {
          repo.deleteAccount(userId,callback)
    }

//    fun getCurrentUser(): FirebaseUser?
//    {
//
//    }



    fun getUserById(
        userId: String,
        callback: (Boolean, String, UserModel?) -> Unit)
    {
        repo.getUserById(userId,callback)
    }


    fun getAllUser(callback: (Boolean, String, List<UserModel>?) -> Unit)
    {
        repo.getAllUser(callback)

    }



}