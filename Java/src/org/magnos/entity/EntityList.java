/* 
 * NOTICE OF LICENSE
 * 
 * This source file is subject to the Open Software License (OSL 3.0) that is 
 * bundled with this package in the file LICENSE.txt. It is also available 
 * through the world-wide-web at http://opensource.org/licenses/osl-3.0.php
 * If you did not receive a copy of the license and are unable to obtain it 
 * through the world-wide-web, please send an email to magnos.software@gmail.com 
 * so we can send you a copy immediately. If you use any of this software please
 * notify me via our website or email, your feedback is much appreciated. 
 * 
 * @copyright   Copyright (c) 2011 Magnos Software (http://www.magnos.org)
 * @license     http://opensource.org/licenses/osl-3.0.php
 * 				Open Software License (OSL 3.0)
 */

package org.magnos.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


public class EntityList extends Entity
{

   public static int DEFAULT_CAPACITY = 16;

   protected Entity[] entities = {};
   protected int entityCount = 0;
   protected boolean inheritVisible;
   protected boolean inheritEnabled;

   public EntityList()
   {
      this( EntityCore.newTemplate(), DEFAULT_CAPACITY );
   }

   public EntityList( int initialCapacity )
   {
      this( EntityCore.newTemplate(), initialCapacity );
   }

   public EntityList( Entity... entities )
   {
      this( EntityCore.newTemplate(), entities.length );

      this.add( entities );
   }

   public EntityList( Template template, Entity... entities )
   {
      this( template, entities.length );

      this.add( entities );
   }

   public EntityList( Template template, int initialCapacity )
   {
      super( template );

      this.entities = new Entity[initialCapacity];
   }

   protected EntityList( Template template, Object[] values, Renderer renderer )
   {
      super( template, values, renderer );
   }

   protected void onEntityAdd( Entity e, int index )
   {
   }

   protected void onEntityRemove( Entity e, int index )
   {
   }

   protected void onEntityUpdated( Entity e, int index, Object updateState )
   {
   }

   public void pad( int count )
   {
      if (entityCount + count >= entities.length)
      {
         int nextCapacity = entities.length + (entities.length >> 1);
         int minimumCapacity = entityCount + count;

         entities = Arrays.copyOf( entities, Math.max( nextCapacity, minimumCapacity ) );
      }
   }

   private void internalAdd( Entity entity )
   {
      entities[entityCount] = entity;

      onEntityAdd( entity, entityCount );

      entityCount++;
   }

   public void add( Entity entity )
   {
      pad( 1 );
      internalAdd( entity );
   }

   public void add( Entity... entityArray )
   {
      pad( entityArray.length );

      for (int i = 0; i < entityArray.length; i++)
      {
         internalAdd( entityArray[i] );
      }
   }

   public void addRange( Entity[] entityArray, int from, int to )
   {
      pad( to - from );

      while (from < to)
      {
         internalAdd( entityArray[from++] );
      }
   }

   public void addAll( EntityList list )
   {
      addRange( list.entities, 0, list.entityCount );
   }

   public void addAll( Collection<Entity> entityCollection )
   {
      pad( entityCollection.size() );

      for (Entity e : entityCollection)
      {
         internalAdd( e );
      }
   }

   public void addAll( Iterator<Entity> iterator )
   {
      while (iterator.hasNext())
      {
         add( iterator.next() );
      }
   }

   public void addAll( Iterable<Entity> iterable )
   {
      addAll( iterable.iterator() );
   }

   public void clean()
   {
      int alive = 0;

      for (int i = 0; i < entityCount; i++)
      {
         final Entity e = entities[i];

         if (e.isExpired())
         {
            onEntityRemove( e, i );
            
            e.delete();
         }
         else
         {
            entities[alive++] = e;
         }
      }

      while (entityCount > alive)
      {
         entities[--entityCount] = null;
      }
   }
   
   @Override
   public boolean delete()
   {
      boolean deletable = super.delete();
      
      if (deletable)
      {
         for (int i = 0 ; i < entityCount; i++)
         {
            entities[i].delete();
         }
      }
      
      return deletable;
   }

   @Override
   public void draw( Object drawState )
   {
      if (visible || !inheritVisible)
      {
         final boolean draw = (visible && renderer != null);
         
         if (draw)
         {
            renderer.drawStart( this, drawState );
            renderer.draw( this, drawState );  
         }
         
         for (int i = 0; i < entityCount; i++)
         {
            final Entity e = entities[i];
            
            if (!e.isExpired())
            {
               entities[i].draw( drawState );   
            }
         }
         
         if (draw)
         {
            renderer.drawEnd( this, drawState );   
         }
      }
   }

   @Override
   public void update( Object updateState )
   {
      if (enabled || !inheritEnabled)
      {
         super.update( updateState );

         if (isExpired())
         {
            return;
         }

         for (int i = 0; i < entityCount; i++)
         {
            final Entity e = entities[i];

            if (!e.isExpired())
            {
               e.update( updateState );

               onEntityUpdated( e, i, updateState );   
            }
         }

         this.clean();   
      }
   }

   public int size()
   {
      return entityCount;
   }

   public Entity at( int index )
   {
      return entities[index];
   }

   public boolean isInheritVisible()
   {
      return inheritVisible;
   }

   public void setInheritVisible( boolean inheritVisible )
   {
      this.inheritVisible = inheritVisible;
   }

   public boolean isInheritEnabled()
   {
      return inheritEnabled;
   }

   public void setInheritEnabled( boolean inheritEnabled )
   {
      this.inheritEnabled = inheritEnabled;
   }

   @Override
   public EntityList clone( boolean deep )
   {
      EntityList clone = cloneState( new EntityList( template, template.createClonedValues( values, deep ), renderer ) );

      clone.inheritEnabled = inheritEnabled;
      clone.inheritVisible = inheritVisible;
      clone.pad( entityCount );

      for (int i = 0; i < entityCount; i++)
      {
         clone.internalAdd( entities[i].clone( deep ) );
      }
      
      return clone;
   }
   
   @Override
   protected int getEntitySize()
   {
      return entityCount + 1;
   }

   @Override
   protected Entity getEntity( int index )
   {
      return (index == 0 ? this : entities[index - 1]);
   }

}
