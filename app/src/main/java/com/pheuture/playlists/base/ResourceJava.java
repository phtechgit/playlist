package com.pheuture.playlists.base;

public class ResourceJava<T>{
    private T data = null;
    private String mesage = null;

    public class Loading{
        private T data;

        public Loading(T data) {
            this.data = data;
        }
    }

    public class Success{
        T data;

        public Success(T data) {
            this.data = data;
        }
    }

    public class Error{
        T data;
        String message;

        public Error(T data) {
            this.data = data;
        }
    }
}
