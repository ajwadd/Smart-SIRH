import { NextResponse } from 'next/server';

export async function POST(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const employeeId = searchParams.get('employeeId') || '';
    const notes = searchParams.get('notes') || '';

    const response = await fetch(`http://localhost:8081/api/attendance/check-in?employeeId=${employeeId}&notes=${encodeURIComponent(notes)}`, {
      method: 'POST',
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
      return NextResponse.json({ message: errorText || 'Échec du pointage.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Check-in proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service de présences.' }, { status: 500 });
  }
}
