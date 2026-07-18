import { NextResponse } from 'next/server';

export async function PATCH(request, { params }) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const { id } = params;

    const response = await fetch(`http://localhost:8081/api/leaves/${id}/approve`, {
      method: 'PATCH',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json',
      },
    });

    if (response.ok) {
      return new NextResponse(null, { status: 204 });
    } else {
      const errorText = await response.text();
      return NextResponse.json({ message: errorText || 'Échec de l\'approbation du congé.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Leave approval proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service d\'approbation.' }, { status: 500 });
  }
}
