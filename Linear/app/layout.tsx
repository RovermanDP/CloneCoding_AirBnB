import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Butler OS",
  description: "Linear-inspired property operations dashboard for the Butler project.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}

