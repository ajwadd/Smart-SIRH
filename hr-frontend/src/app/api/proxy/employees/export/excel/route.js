import { NextResponse } from 'next/server';

export async function GET(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const response = await fetch('http://localhost:8081/api/employees/export/excel', {
      method: 'GET',
      headers: {
        'Authorization': authHeader,
      },
    });

    if (response.ok) {
      const buffer = await response.arrayBuffer();
      const headers = new Headers();
      headers.set('Content-Type', response.headers.get('content-type') || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
      headers.set('Content-Disposition', response.headers.get('content-disposition') || 'attachment; filename=employees_export.xlsx');
      
      return new NextResponse(buffer, {
        status: 200,
        headers,
      });
    } else {
      const errorText = await response.text();
      return NextResponse.json({ message: errorText || 'Échec de l\'export Excel des employés.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Excel Export proxy error:', error);
    return NextResponse.json({ message: 'Erreur lors de la génération de l\'export Excel.' }, { status: 500 });
  }
}
