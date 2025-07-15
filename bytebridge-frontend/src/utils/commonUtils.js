export function getContentType(fileExtension) {
    let contentType = "";

    switch (fileExtension) {
        case 'pdf':
          contentType = 'application/pdf';
          break;
        case 'txt':
          contentType = 'text/plain';
          break;

        // Image types
        case 'jpg':
        case 'jpeg':
          contentType = 'image/jpeg';
          break;
        case 'png':
          contentType = 'image/png';
          break;
        case 'gif':
          contentType = 'image/gif';
          break;
        case 'bmp':
          contentType = 'image/bmp';
          break;
        case 'webp':
          contentType = 'image/webp';
          break;
        case 'svg':
          contentType = 'image/svg+xml';
          break;

        // Code/Text types 
        case 'js':
          contentType = 'text/javascript';
          break;
        case 'ts':
          contentType = 'text/typescript';
          break;
        case 'jsx':
          contentType = 'text/jsx';
          break;
        case 'tsx':
          contentType = 'text/tsx';
          break;
        case 'json':
          contentType = 'application/json';
          break;
        case 'html':
        case 'htm':
          contentType = 'text/html';
          break;
        case 'css':
          contentType = 'text/css';
          break;
        case 'xml':
          contentType = 'text/xml';
          break;
        case 'java':
          contentType = 'text/x-java-source';
          break;
        case 'c':
          contentType = 'text/x-c';
          break;
        case 'cpp':
        case 'cxx':
          contentType = 'text/x-c++src';
          break;
        case 'h':
        case 'hpp':
          contentType = 'text/x-chdr';
          break;
        case 'py':
          contentType = 'text/x-python';
          break;
        case 'rb':
          contentType = 'text/x-ruby';
          break;
        case 'php':
          contentType = 'application/x-php';
          break;
        case 'go':
          contentType = 'text/x-go';
          break;
        case 'sh':
          contentType = 'application/x-sh';
          break;
        case 'yml':
        case 'yaml':
          contentType = 'application/x-yaml';
          break;
        case 'md':
          contentType = 'text/markdown';
          break;

        // Audio/Video
        case 'mp3':
          contentType = 'audio/mpeg';
          break;
        case 'wav':
          contentType = 'audio/wav';
          break;
        case 'ogg':
          contentType = 'audio/ogg';
          break;
        case 'mp4':
          contentType = 'video/mp4';
          break;
        case 'webm':
          contentType = 'video/webm';
          break;

        default:
          contentType = 'application/octet-stream';
      }
      
    return contentType;
}