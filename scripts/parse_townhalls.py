"""
Script: parse_townhalls.py

Este script está hecho para automatizar el proceso de generación de un archivo
JSON que contiene la lista jerárquica de provincias y municipios de España, a
partir del fichero oficial del INE (Instituto Nacional de Estadística).

¿Qué hace?
----------
1. Descarga automáticamente el fichero `codmun25.xls` desde la web del INE.
2. Extrae y normaliza los nombres de municipios (ej. "Torres de Cotillas, Las"
    -> "Las Torres de Cotillas").
3. Agrupa los municipios por provincia y genera un JSON estructurado.
4. Guarda el JSON directamente en la carpeta `app/src/main/res/raw` del
    proyecto Android.
5. Opcionalmente, permite conservar o eliminar el archivo Excel descargado.

Requisitos
----------
- Python 3.10 o superior
- Librerias: pandas, openpyxl, requests

Instalación de dependencias
---------------------------
    pip install -r requirements.txt

Uso
---
    python3 parse_townhalls.py

El script preguntará automáticamente si deseas conservar el archivo Excel una
vez terminado.
"""

import pandas as pd
import requests
import os
import sys
from firebase_uploader import FirebaseUploader

# Define constants of the script
EXCEL_URL = "https://www.ine.es/daco/daco42/codmun/diccionario25.xlsx"
EXCEL_FILE = "codmun25.xls"
SERVICE_ACCOUNT = "google-services.json"

PROVINCE_TO_COMMUNITY = {
    "01": ("País Vasco", "16"), "02": ("Castilla-La Mancha", "08"),
    "03": ("Comunidad Valenciana", "10"), "04": ("Andalucía", "01"),
    "05": ("Castilla y León", "07"), "06": ("Extremadura", "11"),
    "07": ("Islas Baleares", "04"), "08": ("Cataluña", "09"),
    "09": ("Castilla y León", "07"), "10": ("Extremadura", "11"),
    "11": ("Andalucía", "01"), "12": ("Comunidad Valenciana", "10"),
    "13": ("Castilla-La Mancha", "08"), "14": ("Andalucía", "01"),
    "15": ("Galicia", "12"), "16": ("Castilla-La Mancha", "08"),
    "17": ("Cataluña", "09"), "18": ("Andalucía", "01"),
    "19": ("Castilla-La Mancha", "08"), "20": ("País Vasco", "16"),
    "21": ("Andalucía", "01"), "22": ("Aragón", "02"),
    "23": ("Andalucía", "01"), "24": ("Castilla y León", "07"),
    "25": ("Cataluña", "09"), "26": ("La Rioja", "13"),
    "27": ("Galicia", "12"), "28": ("Madrid", "14"), "29": ("Andalucía", "01"),
    "30": ("Murcia", "15"), "31": ("Navarra", "17"), "32": ("Galicia", "12"),
    "33": ("Asturias", "03"), "34": ("Castilla y León", "07"),
    "35": ("Canarias", "05"), "36": ("Galicia", "12"),
    "37": ("Castilla y León", "07"), "38": ("Canarias", "05"),
    "39": ("Cantabria", "06"), "40": ("Castilla y León", "07"),
    "41": ("Andalucía", "01"), "42": ("Castilla y León", "07"),
    "43": ("Cataluña", "09"), "44": ("Aragón", "02"),
    "45": ("Castilla-La Mancha", "08"), "46": ("Comunidad Valenciana", "10"),
    "47": ("Castilla y León", "07"), "48": ("País Vasco", "16"),
    "49": ("Castilla y León", "07"), "50": ("Aragón", "02"),
    "51": ("Ceuta", "18"), "52": ("Melilla", "19"),
}

PROVINCE_TO_NAME = {
    '01': 'Araba', '02': 'Albacete', '03': 'Alicante', '04': 'Almeria',
    '05': 'Avila', '06': 'Badajoz', '07': 'Islas Baleares', '08': 'Barcelona',
    '09': 'Burgos', '10': 'Caceres', '11': 'Cadiz', '12': 'Castellon',
    '13': 'Ciudad Real', '14': 'Cordoba', '15': 'Coruna', '16': 'Cuenca',
    '17': 'Girona', '18': 'Granada', '19': 'Guadalajara', '20': 'Gipuzkoa',
    '21': 'Huelva', '22': 'Huesca', '23': 'Jaen', '24': 'Leon', '25': 'Lleida',
    '26': 'La Rioja', '27': 'Lugo', '28': 'Madrid', '29': 'Malaga',
    '30': 'Murcia', '31': 'Navarra', '32': 'Ourense', '33': 'Asturias',
    '34': 'Palencia', '35': 'Las Palmas', '36': 'Pontevedra',
    '37': 'Salamanca', '38': 'Santa Cruz de Tenerife', '39': 'Cantabria',
    '40': 'Segovia', '41': 'Sevilla', '42': 'Soria', '43': 'Tarragona',
    '44': 'Teruel', '45': 'Toledo', '46': 'Valencia', '47': 'Valladolid',
    '48': 'Bizkaia', '49': 'Zamora', '50': 'Zaragoza', '51': 'Ceuta',
    '52': 'Melilla'
}


def normalizeName(name):
    if not isinstance(name, str):
        return name

    parts = name.split(", ")
    if len(parts) == 2:
        article = parts[1].strip()
        base = parts[0].strip()
        return f"{article} {base}"

    return name.strip()


def download_excel():
    if not os.path.exists(EXCEL_FILE):
        print("Downloading xls file from INE...")
        r = requests.get(EXCEL_URL)
        with open(EXCEL_FILE, "wb") as f:
            f.write(r.content)
        print("Download completed")
    else:
        print("Excel already exists, using local version")


def parse_excel():
    df = pd.read_excel(EXCEL_FILE)
    df.columns = df.columns.str.upper()

    communities = {}

    for a, row in df.iterrows():
        # Limpieza y formateo
        try:
            cpro = str(int(row["UNNAMED: 1"])).zfill(2)
            cmun = str(int(row["UNNAMED: 2"])).zfill(3)
        except (ValueError, TypeError):
            continue  # Ignorar filas inválidas

        # Ignorar filas sin provincia o municipio válido
        if cpro not in PROVINCE_TO_COMMUNITY:
            continue

        name = normalizeName(row["UNNAMED: 4"])
        if not name or not isinstance(name, str):
            continue

        community_name, community_code = PROVINCE_TO_COMMUNITY[cpro]

        if community_code not in communities:
            communities[community_code] = {
                "code": community_code,
                "name": community_name,
                "provinces": {}
            }

        community = communities[community_code]

        if cpro not in community["provinces"]:
            community["provinces"][cpro] = {
                "code": cpro,
                "name": PROVINCE_TO_NAME.get(cpro, "Unknown"),
                "municipalities": []
            }

        community["provinces"][cpro]["municipalities"].append({
            "code": f'{cpro}{cmun}',
            "name": name
        })

    return communities


def main():
    download_excel()
    communities = parse_excel()
    uploader = FirebaseUploader(SERVICE_ACCOUNT)
    uploader.upload_hierarchy(communities)


if __name__ == "__main__":
    if sys.version_info < (3, 10):
        print("Python 3.10 or superior is required.")
        sys.exit(1)
    main()
