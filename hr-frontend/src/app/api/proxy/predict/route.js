import { NextResponse } from 'next/server';

export async function POST(request) {
  try {
    const body = await request.json();

    const response = await fetch('http://localhost:8083/predict', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    const data = await response.json();
    return NextResponse.json(data, { status: response.status });
  } catch (error) {
    console.error('Predict proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service de Machine Learning' }, { status: 500 });
  }
}
