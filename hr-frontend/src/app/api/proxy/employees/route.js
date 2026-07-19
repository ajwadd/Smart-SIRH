import { NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';

export async function GET(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const keyword = searchParams.get('keyword') || '';
    const size = searchParams.get('size') || '100';

    const response = await fetch(`http://localhost:8081/api/employees?keyword=${keyword}&size=${size}`, {
      method: 'GET',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json',
      },
      cache: 'no-store'
    });

    if (response.ok) {
      const data = await response.json();
      return NextResponse.json(data, { status: response.status });
    } else {
      const errorText = await response.text();
      return NextResponse.json({ message: errorText || 'Échec du chargement des employés.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Employees proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service d\'employés.' }, { status: 500 });
  }
}

export async function POST(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const body = await request.json();

    const response = await fetch('http://localhost:8081/api/employees', {
      method: 'POST',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (response.ok) {
      const data = await response.json();
      return NextResponse.json(data, { status: response.status });
    } else {
      const errorText = await response.text();
      return NextResponse.json({ message: errorText || 'Échec de la création de l\'employé.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Employees POST proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service de création d\'employés.' }, { status: 500 });
  }
}
