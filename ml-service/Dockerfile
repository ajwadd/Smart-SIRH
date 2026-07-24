FROM python:3.10-slim

WORKDIR /app

# Installer les dépendances système minimales
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Installer les packages Python directement
RUN pip install --no-cache-dir \
    flask \
    fastapi \
    uvicorn \
    pydantic \
    pandas \
    scikit-learn==1.6.0 \
    joblib \
    shap \
    xgboost \
    imbalanced-learn \
    tzdata

# Copier le code source, config et modèles
COPY . .

EXPOSE 8083

CMD ["python", "app.py"]
