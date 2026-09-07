/**
 * 复制文本。优先使用 Clipboard API，并兼容 HTTP/IP 环境下不可用的情况。
 */
export async function copyText(text: string): Promise<void> {
  if (navigator.clipboard && window.isSecureContext) {
    try {
      await navigator.clipboard.writeText(text)
      return
    } catch {
      // 权限被拒绝时继续使用兼容方案。
    }
  }

  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.readOnly = true
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  textarea.style.top = '0'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.focus()
  textarea.select()
  textarea.setSelectionRange(0, text.length)

  try {
    if (!document.execCommand('copy')) {
      throw new Error('浏览器不支持自动复制')
    }
  } finally {
    document.body.removeChild(textarea)
  }
}
