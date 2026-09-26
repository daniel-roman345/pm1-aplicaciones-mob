# -*- coding: utf-8 -*-
"""
Convierte a hash las contrasenas que la base de datos guarda en texto plano.
El backend valida con check_password_hash (Werkzeug), por lo que un password
en texto plano nunca coincide y el login siempre responde 401.
Ejecutar UNA sola vez, con el entorno virtual activo y el servidor detenido.
"""
import sqlite3
import os
from werkzeug.security import generate_password_hash, check_password_hash

RUTA_DB = os.path.join("database", "ecommerce.db")

def ya_esta_hasheada(valor):
    return valor.startswith("pbkdf2:") or valor.startswith("scrypt:")

def main():
    if not os.path.exists(RUTA_DB):
        print("No encuentro la base de datos en:", os.path.abspath(RUTA_DB))
        print("Ejecuta este script desde la carpeta raiz del proyecto.")
        return

    conexion = sqlite3.connect(RUTA_DB)
    cursor = conexion.cursor()
    cursor.execute("SELECT iD_User, UserName, PasswoRDkey FROM Users")
    usuarios = cursor.fetchall()

    cambiados = 0
    for id_usuario, nombre, password in usuarios:
        if ya_esta_hasheada(password):
            print("[ok]     %s ya tenia hash" % nombre)
            continue
        nuevo = generate_password_hash(password, method="pbkdf2:sha256", salt_length=16)
        cursor.execute(
            "UPDATE Users SET PasswoRDkey = ? WHERE iD_User = ?",
            (nuevo, id_usuario),
        )
        print("[cambio] %s : '%s' -> %s..." % (nombre, password, nuevo[:38]))
        cambiados += 1

    conexion.commit()
    print("\nContrasenas actualizadas:", cambiados)

    print("\n--- Verificacion ---")
    for nombre, password in [("admin", "admin123"), ("juan_perez", "password123")]:
        cursor.execute("SELECT PasswoRDkey FROM Users WHERE UserName = ?", (nombre,))
        fila = cursor.fetchone()
        if fila:
            print("%-12s password correcto -> %s" % (nombre, check_password_hash(fila[0], password)))
            print("%-12s password erroneo  -> %s" % (nombre, check_password_hash(fila[0], "xxxx")))

    conexion.close()

if __name__ == "__main__":
    main()
