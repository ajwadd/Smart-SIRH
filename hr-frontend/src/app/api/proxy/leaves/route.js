import { NextResponse } from 'next/server';

export async function GET(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    // Récupérer la liste des congés (depuis le backend, lister tous les congés est fait sur /leaves)
    const response = await fetch('http://localhost:8081/api/leaves', {
      method: 'GET',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json',
      },
    });

    if (response.ok) {
      const data = await response.json();
      return NextResponse.json(data, { status: response.status });
    } else {
      const errorText = await response.text();
      try {
        const errorJson = JSON.parse(errorText);
        return NextResponse.json(errorJson, { status: response.status });
      } catch (e) {
        return NextResponse.json({ message: errorText || 'Échec du chargement des congés.' }, { status: response.status });
      }
    }
  } catch (error) {
    console.error('Leaves GET proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service de congés.' }, { status: 500 });
  }
}

export async function POST(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const body = await request.json();

    const response = await fetch('http://localhost:8081/api/leaves', {
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
      try {
        const errorJson = JSON.parse(errorText);
        return NextResponse.json(errorJson, { status: response.status });
      } catch (e) {
        return NextResponse.json({ message: errorText || 'Échec de la demande de congé.' }, { status: response.status });
      }
    }
  } catch (error) {
    console.error('Leaves POST proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service de création de congés.' }, { status: 500 });
  }
}
