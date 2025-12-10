package com.example.rojgar.model


    data class UserModel(
        val userId: String = "",
        val email: String = "",
        val firstName: String = "",
        val lastName: String = "",
        val contact: String = "",
        val dob: String = "",
    ) {

        fun toMap(): Map<String, Any?> {
            return mapOf(
                "userId" to userId,
                "email" to email,
                "firstName" to firstName,
                "lastName" to lastName,
                " contact" to contact,
                "dob" to dob,

                )


        }
    }


