const fs = require('fs');
const path = require('path');

function copyFolderSync(from, to) {
  if (!fs.existsSync(to)) {
    fs.mkdirSync(to, { recursive: true });
  }
  fs.readdirSync(from).forEach(element => {
    // Skip WEB-INF to keep static bundle clean
    if (element === 'WEB-INF') return;

    const stat = fs.lstatSync(path.join(from, element));
    if (stat.isFile()) {
      fs.copyFileSync(path.join(from, element), path.join(to, element));
    } else if (stat.isDirectory()) {
      copyFolderSync(path.join(from, element), path.join(to, element));
    }
  });
}

const source = path.join(__dirname, 'src', 'main', 'webapp');
const target = path.join(__dirname, 'public');

console.log('Copying static assets from', source, 'to', target);
copyFolderSync(source, target);
console.log('Static assets prepared for Vercel deployment successfully.');
