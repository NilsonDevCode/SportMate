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
import json
import requests
import os
import sys

# Define constants of the script
EXCEL_URL = "https://www.ine.es/daco/daco42/codmun/diccionario25.xlsx"
EXCEL_FILE = "codmun25.xls"
JSON_FILENAME = "townhalls.json"

# NOTE: Take care when moving this file or changing this path because its
# not dynamic
OUTPUT_PATH = "../app/src/main/res/raw/"

if sys.version_info < (3, 10):
    print("Python 3.10 or superior is required.")
    sys.exit(1)

# Create the directories if don't exist
os.makedirs(OUTPUT_PATH, exist_ok=True)

if not os.path.exists(EXCEL_FILE):
    print("Downloading xls file from INE...")
    r = requests.get(EXCEL_URL)
    with open(EXCEL_FILE, "wb") as f:
        f.write(r.content)
    print("Download completed")
else:
    print("Excel already exists, using local version")

# Read the excel
df = pd.read_excel(EXCEL_FILE)
df.columns = df.columns.str.upper()

province_to_name = {
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


# Make corrections from the INE syntax
def normalizeName(name):
    if not isinstance(name, str):
        return name

    parts = name.split(", ")
    if len(parts) == 2:
        article = parts[1].strip()
        base = parts[0].strip()
        return f"{article} {base}"

    return name.strip()


provinces = {}

# For each row in the file we get the province code, townhall code and name
for _, row in df.iterrows():
    cpro = str(row["UNNAMED: 1"]).zfill(2)
    cmun = str(row["UNNAMED: 2"]).zfill(3)
    name = normalizeName(row["UNNAMED: 4"])

    if cpro not in provinces:
        provinces[cpro] = {
            "code": cpro,
            "name": province_to_name.get(cpro, "Unknown"),
            "towns": []
        }

    provinces[cpro]["towns"].append({
        "code": cmun,
        "name": name
    })

for province in provinces.values():
    province["towns"] = sorted(
        province["towns"], key=lambda m: m["name"].lower()
    )

result = {
    "provinces": sorted(provinces.values(), key=lambda x: x["code"])
}

# We save the json file inside the project
output_file = os.path.join(OUTPUT_PATH, JSON_FILENAME)
with open(output_file, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, indent=2)

print(f"JSON generated successfully in {output_file}")

response = input(
    "\nDo you want to save the downloaded file (.xls)? (y/n): "
).strip().lower()

if response not in ["y", "yes"]:
    try:
        os.remove(EXCEL_FILE)
        print("File deleted successfully.")
    except Exception as e:
        print(f"Couldn't delete the Excel file: {e}")

else:
    print("The Excel file is saved.")
