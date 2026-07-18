import { NextResponse } from 'next/server';

export async function PATCH(request, { params }) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const { id } = params;
    const { searchParams } = new URL(request.url);
    const reason = searchParams.get('reason') || '';

    const response = await fetch(`http://localhost:8081/api/leaves/${id}/reject?reason=${encodeURIComponent(reason)}`, {
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
      return NextResponse.json({ message: errorText || 'Échec du rejet du congé.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Leave rejection proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service de rejet.' }, { status: 500 });
  }
}
