FROM python:3.10-slim

WORKDIR /app

# Installer les dépendances système minimales
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Installer les packages Python directement
RUN pip install --no-cache-dir \
    flask \
    pandas \
    scikit-learn==1.6.0 \
    joblib \
    tzdata

# Copier le code source et le modèle entraîné
COPY app.py .
COPY attrition_model.joblib .

EXPOSE 8083

CMD ["python", "app.py"]
