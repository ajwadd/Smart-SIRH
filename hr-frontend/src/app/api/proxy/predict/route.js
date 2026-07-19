import { NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';

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

export async function GET(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const employeeId = searchParams.get('employeeId') || '';

    if (!employeeId) {
      return NextResponse.json({ message: 'employeeId manquant' }, { status: 400 });
    }

    const response = await fetch(`http://localhost:8082/ai/predict/employee/${employeeId}`, {
      method: 'GET',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json',
      },
      cache: 'no-store'
    });

    if (response.ok) {
      const data = await response.json();
      // Assurer la compatibilité avec le format attendu par le frontend
      const mappedData = {
        ...data,
        risk_level: data.riskLevel,
        factors: data.influencingFactors
      };
      return NextResponse.json(mappedData, { status: response.status });
    } else {
      const errorText = await response.text();
      return NextResponse.json({ message: errorText || 'Échec du calcul d\'attrition.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Predict employee proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service d\'IA' }, { status: 500 });
  }
}
