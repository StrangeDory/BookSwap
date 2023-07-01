package com.example.bookswap.utils

class Book(_name: String, _author: String, _description: String, _comment: String) {


    var name: String = _name
    var author: String = _author
    var description: String = _description
    var comment: String = _comment
    var id: String = "-1"

    constructor(_name: String, _author: String, _description: String, _comment: String, _id: String):
            this(_name, _author, _description, _comment) {
        this.id = _id
    }
}