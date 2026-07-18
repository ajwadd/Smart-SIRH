import { NextResponse } from 'next/server';

export async function POST(request) {
  try {
    const authHeader = request.headers.get('authorization');
    if (!authHeader) {
      return NextResponse.json({ message: 'Token d\'autorisation manquant' }, { status: 401 });
    }

    const formData = await request.formData();
    
    console.log('--- [Next.js Upload Proxy Debug] ---');
    console.log('Authorization Header:', authHeader);
    console.log('EmployeeId field:', formData.get('employeeId'));
    console.log('Type field:', formData.get('type'));
    const file = formData.get('file');
    console.log('File Name:', file ? file.name : 'null', 'Size:', file ? file.size : 'null');

    // Rediriger la requête multipart/form-data vers le backend Spring Boot principal
    const response = await fetch('http://localhost:8081/api/documents/upload', {
      method: 'POST',
      headers: {
        'Authorization': authHeader,
        // Laisser fetch gérer le Content-Type avec le bon boundary pour le multipart/form-data
      },
      body: formData,
    });

    if (response.ok) {
      const data = await response.json();
      return NextResponse.json(data, { status: response.status });
    } else {
      const errorText = await response.text();
      return NextResponse.json({ message: errorText || 'Échec du téléversement du document.' }, { status: response.status });
    }
  } catch (error) {
    console.error('Upload proxy error:', error);
    return NextResponse.json({ message: 'Erreur de connexion au service d\'upload du backend.' }, { status: 500 });
  }
}
