import { NextResponse } from 'next/server';

export async function POST(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const body = await request.json();

    const response = await fetch('http://localhost:8083/predict/anomaly', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (response.ok) {
      const data = await response.json();
      return NextResponse.json(data, { status: response.status });
    } else {
      const errorText = await response.text();
      return NextResponse.json({ message: errorText || 'Échec de la prédiction d\'anomalie.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Anomaly prediction proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service d\'anomalies ML.' }, { status: 500 });
  }
}
