-- Prueba completa de análisis semántico
-- Este archivo debe compilar sin errores

with Ada.Text_IO; use Ada.Text_IO;

procedure TestComplete is
   -- Función declarada antes de ser usada
   function Multiply(X, Y : Integer) return Integer is
      Result : Integer;
   begin
      Result := X;  -- OK: X es un parámetro
      return Result;
   end Multiply;

   A : Integer;
   B : Integer;
   C : Integer;

begin
   A := 10;
   B := 20;
   C := Multiply(A, B);  -- OK: Multiply está declarada arriba
   Put_Line("Test completed successfully");
end TestComplete;

