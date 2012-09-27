#!/bin/bash
so32='x86'
so64='x86_64'
so=$(uname -p)
echo
echo "---------------------------------------------------"

if [ "$so" = $so64 ]; then
  echo "Sistema 64 bits"
  read -p "Deseja instalar o Eclipse 64bits?  [s/N] " install64
  install64=$(echo $install64 | tr [[:upper:]] [[:lower:]] | cut -c1)
  if [ "$install64" = "s" ]; then
    echo "Baixando Eclipse 64 bits..."    
    wget http://espelhos.edugraf.ufsc.br/eclipse//eclipse/downloads/drops4/R-4.2-201206081400/eclipse-SDK-4.2-linux-gtk-x86_64.tar.gz
  else
    read -p "Deseja instalar o Eclipse 32bits?  [s/N] " install32
    install32=$(echo $install32 | tr [[:upper:]] [[:lower:]] | cut -c1)
    if [ "$install32" = "s" ]; then
       echo "Baixando Eclipse 32 bits..."
       wget http://espelhos.edugraf.ufsc.br/eclipse//eclipse/downloads/drops4/R-4.2-201206081400/eclipse-SDK-4.2-linux-gtk.tar.gz
    fi
  fi
else
  so=$so32
fi

if [ "$so" = $so32 ]; then
  echo "Sistema 32 bits"
  read -p "Deseja instalar o Eclipse 32bits?  [s/N] " install32
  install32=$(echo $install32 | tr [[:upper:]] [[:lower:]] | cut -c1)
  if [ "$install32" = "s" ]; then
     echo "Baixando Eclipse 32 bits..."
     wget http://espelhos.edugraf.ufsc.br/eclipse//eclipse/downloads/drops4/R-4.2-201206081400/eclipse-SDK-4.2-linux-gtk.tar.gz
  else
     read -p "Deseja instalar o Eclipse 64bits?  [s/N] " install64
     install64=$(echo $install64 | tr [[:upper:]] [[:lower:]] | cut -c1)
     if [ "$install64" = "s" ]; then
       echo "Baixando Eclipse 64 bits..."
       wget http://eclipse.c3sl.ufpr.br/eclipse/downloads/drops4/R-4.2-201206081400/eclipse-SDK-4.2-linux-gtk-x86_64.tar.gz
       
     fi	
  fi
fi


if [ "$install32" = "s" ]; then 
  tar -xvf eclipse-SDK-4.2-linux-gtk.tar.gz
  cp plugins_hpcfact/* eclipse/plugins/
  nohup ./eclipse/eclipse&
else
  if [ "$install64" = "s" ]; then
    tar -xvf eclipse-SDK-4.2-linux-gtk-x86_64.tar.gz
    cp plugins_hpcfact/* eclipse/plugins/
    nohup ./eclipse/eclipse&
  else
    echo "Script encerrado"
    echo "---------------------------------------------------"  
    echo
    exit
  fi
fi

echo "Fim do script"
echo "---------------------------------------------------"
echo
