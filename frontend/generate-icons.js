#!/usr/bin/env node

/**
 * Script para generar iconos en múltiples resoluciones
 * Requiere: npm install sharp
 * Uso: node generate-icons.js
 */

import sharp from 'sharp';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const SOURCE_IMAGE = path.join(__dirname, 'public', 'logo.png');
const OUTPUT_DIR = path.join(__dirname, 'public');

// Configuración de iconos a generar
const icons = [
  // Favicons estándar
  { name: 'favicon-16x16.png', size: 16 },
  { name: 'favicon-32x32.png', size: 32 },
  { name: 'favicon-48x48.png', size: 48 },
  
  // Apple Touch Icons
  { name: 'apple-touch-icon.png', size: 180 },
  { name: 'apple-touch-icon-152x152.png', size: 152 },
  { name: 'apple-touch-icon-120x120.png', size: 120 },
  { name: 'apple-touch-icon-76x76.png', size: 76 },
  
  // Android/Chrome
  { name: 'icon-192.png', size: 192 },
  { name: 'icon-512.png', size: 512 },
  
  // Windows Tiles
  { name: 'mstile-144x144.png', size: 144 },
  { name: 'mstile-70x70.png', size: 70 },
  { name: 'mstile-150x150.png', size: 150 },
  { name: 'mstile-310x310.png', size: 310 },
];

async function generateIcons() {
  try {
    // Verificar que existe el archivo fuente
    if (!fs.existsSync(SOURCE_IMAGE)) {
      console.error(`❌ Error: No se encontró el archivo fuente: ${SOURCE_IMAGE}`);
      process.exit(1);
    }

    console.log('🎨 Generando iconos profesionales para "Finanzas"...\n');
    
    // Obtener información de la imagen original
    const metadata = await sharp(SOURCE_IMAGE).metadata();
    console.log(`📐 Imagen original: ${metadata.width}x${metadata.height}\n`);

    // Generar cada icono
    for (const icon of icons) {
      const outputPath = path.join(OUTPUT_DIR, icon.name);
      
      await sharp(SOURCE_IMAGE)
        .resize(icon.size, icon.size, {
          fit: 'contain',
          background: { r: 255, g: 255, b: 255, alpha: 0 }
        })
        .png({ quality: 100, compressionLevel: 9 })
        .toFile(outputPath);
      
      console.log(`✅ Generado: ${icon.name} (${icon.size}x${icon.size})`);
    }

    // Generar favicon.ico (usando 32x32 como base)
    console.log('\n🔧 Generando favicon.ico...');
    const icoPath = path.join(OUTPUT_DIR, 'favicon.ico');
    await sharp(SOURCE_IMAGE)
      .resize(32, 32, {
        fit: 'contain',
        background: { r: 255, g: 255, b: 255, alpha: 0 }
      })
      .png()
      .toFile(icoPath.replace('.ico', '-temp.png'));
    
    // Renombrar temporalmente (en entorno real usarías una librería ico)
    fs.renameSync(
      icoPath.replace('.ico', '-temp.png'),
      path.join(OUTPUT_DIR, 'favicon-temp.png')
    );
    
    console.log('⚠️  Nota: Para favicon.ico óptimo, usa una herramienta de conversión online');
    console.log('   o instala "to-ico": npm install -g to-ico');
    console.log('   Comando: to-ico public/favicon-32x32.png > public/favicon.ico\n');

    console.log('✨ ¡Proceso completado exitosamente!\n');
    console.log('📦 Archivos generados:');
    console.log(`   - ${icons.length} iconos PNG en múltiples resoluciones`);
    console.log('   - Listos para PWA, iOS, Android y Windows\n');

  } catch (error) {
    console.error('❌ Error al generar iconos:', error);
    process.exit(1);
  }
}

// Verificar si sharp está instalado
async function main() {
  try {
    await generateIcons();
  } catch (error) {
    console.error('❌ Error al generar iconos:', error);
    process.exit(1);
  }
}

main();
