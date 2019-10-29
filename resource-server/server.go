package main

import (
	"fmt"
	"net/http"
	"os"
	"path/filepath"
)

func main() {
	ex, err := os.Executable()
	if err != nil {
		panic(err)
	}
	exPath := filepath.Dir(ex)
	fmt.Println(exPath)
	fs := http.FileServer(http.Dir(exPath + "/static/"))

	http.Handle("/static/", http.StripPrefix("/static/", fs))
	http.HandleFunc("/api", func(w http.ResponseWriter, r *http.Request) {
		_, _ = fmt.Fprintf(w, "This ok!")
	})

	_ = http.ListenAndServe(":9000", nil)
}
