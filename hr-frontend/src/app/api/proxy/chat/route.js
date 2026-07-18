import { NextResponse } from 'next/server';

export async function POST(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const body = await request.json();

    const response = await fetch('http://localhost:8081/api/chat', {
      method: 'POST',
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (response.ok) {
      const data = await response.json();
      const textResponse = data.response || "";
      return new Response(textResponse, {
        status: response.status,
        headers: { 'Content-Type': 'text/plain; charset=utf-8' },
      });
    } else {
      const data = await response.json().catch(() => null);
      const errorMessage = data?.message || "Erreur inconnue de l'IA";
      return new Response("⚠️ " + errorMessage, {
        status: response.status,
        headers: { 'Content-Type': 'text/plain; charset=utf-8' },
      });
    }
  } catch (error) {
    console.error('Chat proxy error:', error);
    return new Response('Erreur de connexion au service d\'IA sémantique', { status: 500 });
  }
}
