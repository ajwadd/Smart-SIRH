import { NextResponse } from 'next/server';

export async function GET(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const response = await fetch('http://localhost:8081/api/employees/export/pdf', {
      method: 'GET',
      headers: {
        'Authorization': authHeader,
      },
    });

    if (response.ok) {
      const buffer = await response.arrayBuffer();
      const headers = new Headers();
      headers.set('Content-Type', response.headers.get('content-type') || 'application/pdf');
      headers.set('Content-Disposition', response.headers.get('content-disposition') || 'attachment; filename=employees_export.pdf');
      
      return new NextResponse(buffer, {
        status: 200,
        headers,
      });
    } else {
      const errorText = await response.text();
      return NextResponse.json({ message: errorText || 'Échec de l\'export PDF des employés.' }, { status: response.status });
    }
  } catch (error) {
    console.error('PDF Export proxy error:', error);
    return NextResponse.json({ message: 'Erreur lors de la génération de l\'export PDF.' }, { status: 500 });
  }
}
