import { NextResponse } from 'next/server';

export async function GET(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const response = await fetch('http://localhost:8081/api/dashboard/summary', {
      method: 'GET',
      headers: {
        'Authorization': authHeader,
      },
    });

    if (response.status === 204) {
      return new Response(null, { status: 204 });
    }

    if (response.ok) {
      const data = await response.json();
      return NextResponse.json(data, { status: response.status });
    } else {
      const errorText = await response.text().catch(() => '');
      return NextResponse.json({ message: errorText || 'Échec du chargement des KPIs.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Dashboard proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au serveur d\'API' }, { status: 500 });
  }
}
